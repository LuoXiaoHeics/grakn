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

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

def graknlabs_build_tools():
    git_repository(
        name = "graknlabs_build_tools",
        remote = "https://github.com/graknlabs/build-tools",
        commit = "d2be22150c8ca386162e0c49d3f82785a11e8d98", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_build_tools
    )

def graknlabs_graql():
     git_repository(
         name = "graknlabs_graql",
         remote = "https://github.com/graknlabs/graql",
         tag = "1.0.0",
     )

def graknlabs_client_java():
     git_repository(
         name = "graknlabs_client_java",
         remote = "https://github.com/graknlabs/client-java",
         commit = "324c7e52449202ad0e5bebb973928df776b6d694",
     )

def graknlabs_benchmark():
    git_repository(
        name = "graknlabs_benchmark",
        remote = "https://github.com/graknlabs/benchmark.git",
        commit = "ceb5a2ebb71ee526d788fb4b17a104a6669d4b70" # keep in sync with protocol changes
    )