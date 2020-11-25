# Custom UI Reporter

## Overview

Run this simple ReactJS app on your IBM Social Program Management (SPM) installation to identify and extract any custom UI configuration in your project. This information is useful to SPM, to help us to plan and support the evolution of the SPM UI. It is useful for you, to help you to efficiently update or upgrade the SPM UI for your project.

The app ignores the standard locations that contain the default SPM UI configuration files, and searches for custom CSS, JavaScript, and domain Java(TM) renderer files in the project.

> Note: For privacy and security, no other information is extracted and no information is requested from any database in the system.

The custom files are copied into an archive file, so you can copy them to a local development environment to recreate the customisation.


## Running the app

You’ll need [node.js 10 or later](https://nodejs.org) to run the app.

Download and extract the app, which consists of the `configuration.json` and `index.js` files.

The `configuration.json` configuration file lists all of the locations to exclude from your search. By default, the standard locations for SPM UI configuration files. You can update this file to exclude other locations if needed.

Run the app with the following command from the extracted directory:

```node index.js```

The custom files are copied into the `Results` directory. The file struture of the copied files is retained, so you can copy them into your local development environment.

## What files are copied?

- For CSS and JavaScript files, we copy any files with the extensions `.css` and `.js`. These are mainly in the `webclient` component, but we also search the `EJBServer` component for IEG customisations, which are in the `EJBServer/component/***/data` directory. For example, ` EJBServer/component/***/data/my-custom.css`.

- For domain renderers, we search for files called `DomainsConfig.xml`, find instances of `view-renderer`, `edit-renderer`, or `select-renderer`, and copy the associated Java file that is defined in the `class` attribute.
