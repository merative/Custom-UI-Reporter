//requiring path and fs modules
const { promisify } = require("util");
const glob = promisify(require("glob"));
const path = require("path");
const fs = require("fs-extra");
const { DOMParser } = require("xmldom");
const cliProgress = require("cli-progress");
var archiver = require("archiver");
const { isConfigurationJsonValid } = require("./validations");

const EJB_SERVER = "EJBServer";
const WEB_CLIENT = "webclient";
let componentsToSearch;
const TEMP_DIR = path.join(__dirname, "results", "temp")
let filesCounter = 0;


let configurations;
async function getConfigurations() {
    const configurations = await fs.readJSON(
        path.join(__dirname, "configuration.json")
    );

    const {ejbServerComponents, webClientComponents } = configurations;

    const componentsToSearch = new Set();
    fs.readdirSync(ejbServerComponents).forEach(file => {
        if(!configurations.skipComponents.includes(file)){
            componentsToSearch.add(file);
        }        
    });

    fs.readdirSync(webClientComponents).forEach(file => {
        if(!configurations.skipComponents.includes(file)){
            componentsToSearch.add(file);
        }        
    });

    configurations.componentsToSearch = [...componentsToSearch].join('|');

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
                " |- Progress: {percentage}%" +
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
                filesCounter++;
                await fs.copy(file, resultsFilePath);
            }
        })
    );
};

function getDomainPattern() {
    let domainSearchPattern;
    const { componentsToSearch } = configurations;
    if (componentsToSearch && componentsToSearch.length > 0) {
        domainSearchPattern = `/*(${componentsToSearch})/DomainsConfig.xml`;
    } else {
        domainSearchPattern = `/**/DomainsConfig.xml`;
    }
    return domainSearchPattern;
}

function getCSSPattern() {
    const {componentsToSearch} = configurations;

    let cssPattern;
    if (componentsToSearch && componentsToSearch.length > 0) {
        cssPattern = `/*(${componentsToSearch})/**/*.css`;
    } else {
        cssPattern = "/**/*.css";
    }
    return cssPattern;
}

function getJSPattern() {
    let jsPattern;
    const {componentsToSearch} = configurations;
    if (componentsToSearch && componentsToSearch.length > 0) {
        jsPattern = `/*(${componentsToSearch})/**/*.js`;
    } else {
        jsPattern = "/**/*.js";
    }
    return jsPattern;
}

const copyJavaRenderers = async ({ webClientComponents }) => {
    const files = await glob(webClientComponents + getDomainPattern());

    progressBarCli.setTotal(progressBarCli.getTotal() + files.length);
    await copyFileToResultsDir(files, WEB_CLIENT);
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
            progressBarCli.increment();
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
            progressBarCli.stop();
            console.log(`\n ${filesCounter} files found. Generating a zip file. \n`)
            console.log(` Zip file created: ${zipFullPath} `);
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

        const isValid = await isConfigurationJsonValid();

       if(!isValid) return;

        configurations = await getConfigurations();
        const { ejbServerComponents, webClientComponents } = configurations;
        // EjbServer
        var start = new Date();
        console.log("Searching for custom UI files - JavaScript, CSS and Domain Java Renderers`) \n");

        createResultsDirectory();

        // task that can be executed em parallel
        const steps = [
            findAndCopyFiles(ejbServerComponents + getCSSPattern(), EJB_SERVER),
            findAndCopyFiles(ejbServerComponents + getJSPattern(), EJB_SERVER),
            findAndCopyFiles(webClientComponents + getCSSPattern(), WEB_CLIENT),
            findAndCopyFiles(webClientComponents + getJSPattern(), WEB_CLIENT),
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
        progressBarCli.stop();
        console.log(error);
    }
};

run();
