#
# GRAKN.AI - THE KNOWLEDGE GRAPH
# Copyright (C) 2018 Grakn Labs Ltd
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

package(default_visibility = ["//visibility:__pkg__"])

load("@graknlabs_build_tools//distribution/maven:rules.bzl", "assemble_maven", "deploy_maven")
load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "daemon",
    srcs = glob(["**/*.java"]),
    deps = [
        # Package dependencies
        "//daemon/executor:grakn-daemon-executor",

        # Grakn Core dependencies
        "//common",

        # External dependencies
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/ch/qos/logback:logback-classic",
        "//dependencies/maven/artifacts/ch/qos/logback:logback-core",
        "//dependencies/maven/artifacts/org/codehaus/janino:janino",
    ],
    resources = ["LICENSE"],
    resource_strip_prefix = "daemon",
    visibility = ["//server:__pkg__"],
    tags = ["maven_coordinates=io.grakn.core:daemon:{pom_version}"],
)

assemble_maven(
    name = "assemble-maven",
    target = ":daemon",
    package = "daemon",
    version_file = "//:VERSION",
    workspace_refs = "@graknlabs_grakn_core_workspace_refs//:refs.json"
)

deploy_maven(
    name = "deploy-maven",
    target = ":assemble-maven"
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":daemon"
    ],
)