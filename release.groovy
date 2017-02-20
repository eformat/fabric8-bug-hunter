#!/usr/bin/groovy
/*
 *
 *   Copyright (c) 2016 Red Hat, Inc.
 *
 *   Red Hat licenses this file to you under the Apache License, version
 *   2.0 (the "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *   implied.  See the License for the specific language governing
 *   permissions and limitations under the License.
 */


def updateDependencies(source) {

    def properties = []
    properties << ['<vertx-core.version>', 'io/vertx/vertx-core']

    updatePropertyVersion {
        updates = properties
        repository = source
        project = 'fabric8io/fabric8-bug-hunter'
    }
}

def stage() {
    return stageProject {
        project = 'fabric8io/fabric8-bug-hunter'
        useGitTagForNextVersion = true
    }
}

def approveRelease(project) {
    def releaseVersion = project[1]
    approve {
        room = null
        version = releaseVersion
        console = null
        environment = 'fabric8'
    }
}

def release(project) {
    releaseProject {
        stagedProject = project
        useGitTagForNextVersion = true
        helmPush = false
        groupId = 'io.fabric8'
        githubOrganisation = 'fabric8io'
        artifactIdToWatchInCentral = 'fabric8-bug-hunter'
        artifactExtensionToWatchInCentral = 'jar'
    }
}

def mergePullRequest(prId) {
    mergeAndWaitForPullRequest {
        project = 'fabric8io/fabric8io/fabric8-bug-hunter'
        pullRequestId = prId
    }

}
/* will enable it once I have asciidoc
def website(stagedProject) {
    Model m = readMavenPom file: 'pom.xml'
    def projectArtifactId = m.artifactId
    genWebsiteDocs {
        project = stagedProject
        artifactId = projectArtifactId
    }
}
*/
return this
