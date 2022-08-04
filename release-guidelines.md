# Carbon 4.7.x Release Process

## Pre Release Tasks
* Check whether upstream projects needs a release before releasing kernel. Kernel depends on wso2-axis2, wso2-axiom, and wso2-xmlschema (You need to check the PRs and the commits of these repos).
* Get the latest test automation framework release. 
  Github Repo: https://github.com/wso2/carbon-platform-integration
* Do a FindSecurityBugs and ZAP scans and review the report. Send the report to security@wso2.com to get it reviewed/approved by the platform security team.

## Release Tasks
1. Create a release branch from the 4.7.x branch.
2. Update the distribution/product/modules/distribution/release-notes.html for the latest release. We need to include the new features, new github filters for issues fixed for the current release and known issues.
    * Change “All fixed issues have been recorded at” section 
    * Change “All known issues have been recorded at” section 
    * Change “Wiki:” section 
3. Verify  carbon.product, filter.properties, README.txt, bin/README.txt, release-notes.html, INSTALL.txt, about.html - check dates, hard-coded versions etc. (ex. 4420, 4.4.20, 2017)
  Following are the changes needed for the files.
    * Update core/org.wso2.carbon.ui/src/main/resources/web/docs/about.html
        * Ex: `<h1>Version 4.7.19</h1>` -> `<h1>Version 4.7.20</h1>`
    * Update distribution/kernel/carbon.product
        * Change product version Ex: 4.7.19.SNAPSHOT -> 4.7.20
        * Change runtime version Ex: 4.7.19.SNAPSHOT -> 4.7.20
    * Update distribution/kernel/src/assembly/filter.properties
        * Change carbon.version Ex: 4.7.19 -> 4.7.20
    * Update distribution/product/modules/distribution/INSTALL.txt
        * Change the version
    * Update distribution/product/modules/distribution/LICENSE.txt
    * Update README.txt, bin/README.txt
        * Change the versions
    * Update distribution/product/modules/distribution/src/assembly/filter.properties
        * Change product.version
        * Change carbon.product.version
        * Change carbon.version
4. Verify all the scripts in the CARBON_HOME/bin/ folder
5. Use maven release plugin with release builder or jenkins builder to release the RC1 of the kernel. The WSO2 release process can be found here.
https://docs.wso2.com/display/Carbon450/Releasing+a+Git+Repository
Note: The tag should be RC1, but that should not be included for the product version.
Uncheck 'Close and Release Nexus Staging Repository'
6. Once the jenkins builder pass, we have to need to Close the Nexus Staging Repository and get the repository link.
7. Once release is done, call for the release vote (send an email to dev@wso2.com) and wait for 72h.
8. If there are no negative votes with in 72 hours, close the vote (reply to the same email)
Note: If there are negative votes, add the changes and do the next RC version.(Go to step 5)
9. If the vote passes, add the release Nexus Staging Repository and Tag from the RC tag that was passed, to the git repo (Ex: v4.7.20)
10. In GitHub, draft a new release with the tag, give a proper title and release.
    
## Post Release Tasks
* Change the version in the carbon.product to have the snapshot of the next version.
* Merge the release branch to the relevant development branch(4.7.x).
* Send the release mail.

