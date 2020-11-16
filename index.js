//requiring path and fs modules
const { promisify } = require("util");
const glob = promisify(require("glob"));
const path = require("path");
const fs = require("fs-extra");
const { DOMParser } = require("xmldom");
const cliProgress = require("cli-progress");
var archiver = require("archiver");


const CSS_FILES_PATTERN = "/**/*.css";
const JS_FILES_PATTERN = "/**/*.js";
const EJB_SERVER = "EJBServer";
const WEB_CLIENT = "webclient";
const TEMP_DIR = path.join(__dirname, "results", "temp")


let configurations;
async function getConfigurations() {
    const configurations = await fs.readJSON(
        path.join(__dirname, "configuration.json")
    );
    return configurations;
}

const findAndCopyFiles = async (path, baseDir) => {
    const files = await glob(path);
    await copyFileToResultsDir(files, baseDir);
};

const progressBarCli = createProgressBar();

function createProgressBar() {
    return new cliProgress.SingleBar(
        {
            format:
                " |- Searching for artefacts in the components files: {percentage}%" +
                " - " +
                "|| {bar} ||",
            fps: 5,
            barsize: 30,
        },
        cliProgress.Presets.shades_classic
    );
}

const copyFileToResultsDir = async function (files, baseDir) {
    await Promise.all(
        files.map(async (file) => {
            const resultsFilePath = path.join(TEMP_DIR,
                file.substring(file.indexOf(baseDir))
            );
            const fileExists = await fs.pathExists(resultsFilePath)
            if (!fileExists) {
                await fs.copy(file, resultsFilePath);
            }
        })
    );
};

function getDomainPattern() {
    let domainSearchPattern;
    const { skipComponents } = configurations;
    if (skipComponents && skipComponents.length > 0) {
        const skipComponents = configurations.skipComponents.join("|");
        domainSearchPattern = `/!(*${skipComponents})/DomainsConfig.xml`;
    } else {
        domainSearchPattern = `/**/DomainsConfig.xml`;
    }
    return domainSearchPattern;
}

const copyJavaRenderers = async ({ webClientComponents }) => {
    const files = await glob(webClientComponents + getDomainPattern());
    return Promise.all(
        files.map(async (filePath) => {
            const data = await fs.readFile(filePath, "UTF-8");
            const doc = new DOMParser().parseFromString(data);
            const elements = doc.getElementsByTagName("dc:plug-in");

            for (let i = 0; i < elements.length; i++) {
                const nameAttr = elements[i].getAttribute("name");

                if (nameAttr.indexOf("renderer") > 0) {
                    const className = elements[i].getAttribute("class");
                    if (className) {
                        const pathToCopy = className.split(".").join("/");
                        await findAndCopyFiles(
                            webClientComponents + "/**/javasource/" + pathToCopy + ".java",
                            WEB_CLIENT
                        );
                    }
                }
            }
        })
    );
};

function createZipFile() {
    return new Promise((resolve) => {
        const zipDir = path.join(__dirname, "results");
        const zipFullPath = path.join(zipDir, "results.zip");

        const output = fs.createWriteStream(zipFullPath);
        const archive = archiver("zip", {
            zlib: { level: 9 }, // Sets the compression level.
        });

        // listen for all archive data to be written
        // 'close' event is fired only when a file descriptor is involved
        output.on("close", function () {
            progressBarCli.increment();
            console.log(archive.pointer() + " total bytes");
            console.log(`zip file archive  ${zipFullPath} `);
            resolve();
        });

        archive.pipe(output);

        archive.directory(TEMP_DIR, false);
        archive.finalize();
    });
}

function createResultsDirectory() {
    const resultsFolder = path.join(__dirname, "results", "temp");
    if (!fs.pathExistsSync(resultsFolder)) {
        fs.mkdirSync(resultsFolder, { recursive: true });
    }
}

const run = async () => {
    try {
        configurations = await getConfigurations();
        const { ejbServerComponents, webClientComponents } = configurations;
        // EjbServer
        var start = new Date();

        createResultsDirectory();

        // task that can be executed em parallel
        const steps = [
            findAndCopyFiles(ejbServerComponents + CSS_FILES_PATTERN, EJB_SERVER),
            findAndCopyFiles(ejbServerComponents + JS_FILES_PATTERN, EJB_SERVER),
            findAndCopyFiles(webClientComponents + CSS_FILES_PATTERN, WEB_CLIENT),
            findAndCopyFiles(webClientComponents + JS_FILES_PATTERN, WEB_CLIENT),
            copyJavaRenderers({ webClientComponents }),
        ].map((e) =>
            e.catch((e) => console.log(e)).then(() => progressBarCli.increment())
        );

        progressBarCli.start(steps.length + 1, 0);
        await Promise.all(steps);

        await createZipFile();
        await fs.remove(TEMP_DIR);

        progressBarCli.stop();
    } catch (error) {
        console.log(error);
    }
};

run();
