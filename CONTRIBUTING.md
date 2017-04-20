## Contributing to the Nitrite Database

Thank you for your interest in contributing to the Nitrite Database.

We are building this software together and strongly encourage contributions from the community that are within the guidelines set forth 
below. 

Bug Fixes and New Features
--------------------------

Before starting to write code, look for existing [issues](https://github.com/dizitart/nitrite-database/issues) or 
[create one](https://github.com/dizitart/nitrite-database/issues/new) for your bug, issue, or feature request. This helps the community 
avoid working on something that might not be of interest or which has already been addressed.

Pull Requests
-------------

Pull requests should generally be made against the master (default) branch and include relevant tests, if applicable. 

Code should compile and tests should pass under all Java versions that Nitrite supports. Currently Nitrite supports a minimum version of Java 8.  
Please run './gradlew test' to confirm. By default, running the tests requires 

*   that you started a mongod server on localhost, listening on the default port and and ran below command to configure the test user
        
        db.getSiblingDB('benchmark').createUser({user: 'bench', pwd: 'bench', roles: [{role: 'readWrite', db: 'benchmark'}, {role: 'dbAdmin', db: 'benchmark'}]})
        
*   that you have installed Android SDK 24.4.1 and ANDROID_HOME is setup to the proper location

The results of pull request testing will be appended to the request. If any tests do not pass, or relevant tests are not included, the 
pull request will not be considered. 

Talk To Us
----------

If you want to work on something or have questions / complaints please talk to us at [gitter](https://gitter.im/dizitart/nitrite-database).