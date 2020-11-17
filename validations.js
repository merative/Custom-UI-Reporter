const fs = require("fs-extra");
const path = require("path");

async function isConfigurationJsonValid() {

    const configurationsFileLocation = path.join(__dirname, "configuration.json");

    if (!fs.existsSync(configurationsFileLocation)) {
        console.log(`[configurations.json] The file configurations.json is missing`);
        return false;
    }

    async function getConfigurations() {
        const configurations = await fs.readJSON(configurationsFileLocation
        );
        return configurations;
    }

    const errorsMessages = [];
    const { ejbServerComponents, webClientComponents, skipComponents } = await getConfigurations();



    if (!webClientComponents) {
        errorsMessages.push(`[configurations.json] The field "webClientComponents" is missing in the file configurations.json`);
    }


    if (!ejbServerComponents) {
        errorsMessages.push(`[configurations.json] The field "ejbServerComponents" is missing in the file configurations.json`);
    }


    if (skipComponents && !Array.isArray(skipComponents)) {
        errorsMessages.push(`[configurations.json] The field "skipComponents" in the file configurations.json must be an array of strings`);
    }

    // test paths
    if (webClientComponents && !fs.existsSync(webClientComponents)) {
        errorsMessages.push(`[configurations.json] The path in "webClientComponents" does not exist. Update the file configurations.json`);
    }

    if (ejbServerComponents && !fs.existsSync(ejbServerComponents)) {
        errorsMessages.push(`[configurations.json] The path in "ejbServerComponents" does not exist. Update the file configurations.json`);
    }

    if (errorsMessages.length) {
        console.error("Errors found:")
        errorsMessages.forEach(message => {
            console.error(`* ${message}`);
        })
        return false;
    }
    return true;
}

module.exports = { isConfigurationJsonValid };