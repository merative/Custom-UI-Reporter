//requiring path and fs modules
const { promisify } = require("util");
const glob = promisify(require("glob"));
const path = require("path");
const fs = require("fs-extra");
const { DOMParser } = require("xmldom");
const cliProgress = require("cli-progress");

let configurations;

async function getConfigurations() {
  const configurations = await fs.readJSON(
    path.join(".", "configuration.json")
  );
  return configurations;
};

// Nice to have - Add a blacklist of components we don't want.

const CSS_FILES_PATTERN = "/**/*.css";
const JS_FILES_PATTERN = "/**/*.js";
const EJB_SERVER = "EJBServer";
const WEB_CLIENT = "webclient";

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
      const resultsFilePath = path.join(
        __dirname,
        "results",
        file.substring(file.indexOf(baseDir))
      );
      await fs.copy(file, resultsFilePath);
    })
  );
};

const copyJavaRenderers = async ({ webClientComponents }) => {
  const skipComponents = configurations.skipComponents.join("|");
  const files = await glob(
    webClientComponents +
      `/!(*${skipComponents})/DomainsConfig.xml`
  );
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

const run = async () => {
  configurations = await getConfigurations();
  const { ejbServerComponents, webClientComponents } = configurations;
  // EjbServer
  var start = new Date();

  console.log("...Copping files: " + start);

  const steps = [
    findAndCopyFiles(ejbServerComponents + CSS_FILES_PATTERN, EJB_SERVER),
    findAndCopyFiles(ejbServerComponents + JS_FILES_PATTERN, EJB_SERVER),
    findAndCopyFiles(webClientComponents + CSS_FILES_PATTERN, WEB_CLIENT),
    findAndCopyFiles(webClientComponents + JS_FILES_PATTERN, WEB_CLIENT),
    copyJavaRenderers({ webClientComponents }),
  ].map((e) => e.then(() => progressBarCli.increment()));

  progressBarCli.start(steps.length, 0);

  await Promise.all(steps);

  var end = new Date();

  progressBarCli.stop();
  console.log(`Finished`, start, `End:`, end);
};

run();
