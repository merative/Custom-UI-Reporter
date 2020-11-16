//requiring path and fs modules
const { promisify } = require('util');
const glob = promisify(require('glob'));
const path = require('path')
const fs = require('fs-extra');
const { DOMParser } = require('xmldom')

const ejbServerComponents = `/Users/lucianodantas/Dev/SPMEntmods_7011/SPM-EntMods/EJBServer/components`;
const webClientComponents = `/Users/lucianodantas/Dev/SPMEntmods_7011/SPM-EntMods/webclient/components`;
const CSS_FILES_PATTERN = '/**/*.css';
const JS_FILES_PATTERN = '/**/*.js';
const EJB_SERVER = 'EJBServer';
const WEB_CLIENT = 'webclient';

const findAndCopyFiles = async (path, baseDir) => {
    const files = await glob(path);
    console.log('files found', JSON.stringify(files));
    await copyFileToResultsDir(files, baseDir)
}

const copyFileToResultsDir = async function (files, baseDir) {
    for (file of files) {
        const resultsFilePath = path.join(__dirname, 'results', file.substring(file.indexOf(baseDir)))
        await fs.copy(file, resultsFilePath);
    };
};

const copyJavaRenderers = async () => {
    const files = await glob(webClientComponents + '/**/DomainsConfig.xml');

    for (const filePath of files) {
        const data = await fs.readFile(filePath, 'UTF-8');
        const doc = new DOMParser().parseFromString(data);
        const elements = doc.getElementsByTagName('dc:plug-in');

        for (let i = 0; i < elements.length; i++) {
            const nameAttr = elements[i].getAttribute('name');

            if (nameAttr.indexOf('renderer') > 0) {
                const className = elements[i].getAttribute('class');
                if (className) {
                    const pathToCopy = className.split('.').join('/');
                    await findAndCopyFiles(webClientComponents + '/**/javasource/' + pathToCopy + '.java', WEB_CLIENT);
                }
            }
        }
    };
}


const run = async () => {
    //ejbServer
    await findAndCopyFiles(ejbServerComponents + CSS_FILES_PATTERN, EJB_SERVER);
    await findAndCopyFiles(ejbServerComponents + JS_FILES_PATTERN, EJB_SERVER);

    // // webclient
    await findAndCopyFiles(webClientComponents + CSS_FILES_PATTERN, WEB_CLIENT);
    await findAndCopyFiles(webClientComponents + JS_FILES_PATTERN, WEB_CLIENT);
    await copyJavaRenderers();
}

run();