const fs = require("fs-extra");
const path = require("path");

async function isConfigurationJsonValid(){

    const configurationsFileLocation =  path.join(__dirname, "configuration.json");

    if(!fs.existsSync(configurationsFileLocation)){
        console.log(`The configurations.json file is missing`);
        return false;
    }

    async function getConfigurations() {
        const configurations = await fs.readJSON(configurationsFileLocation
        );
        return configurations;
    }
    
    const errorsMessages = [];
    const {ejbServerComponents, webClientComponents, skipComponents } = await getConfigurations();



    if(!webClientComponents){
        errorsMessages.push(  `The field "webClientComponents" is missing`);
    }


    if(!ejbServerComponents){
        errorsMessages.push(  `The field "ejbServerComponents" is missing`);
    }


    if(skipComponents && !Array.isArray(skipComponents)){
        errorsMessages.push(`The field "skipComponents" must be an array of string`);
    }

    // test paths
    if(webClientComponents  && !fs.existsSync(webClientComponents)){
        errorsMessages.push(`The path in "webClientComponents" does not exist`);
    }

    if(ejbServerComponents  && !fs.existsSync(ejbServerComponents)){
        errorsMessages.push(`The path in "ejbServerComponents" does not exist`);
    }

    if(errorsMessages.length){
        console.error("Errors found:")
        errorsMessages.forEach( message => {
            console.error(`* ${message}`);
        })
        return false;
    }
    return true;
}

module.exports = { isConfigurationJsonValid };