#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

from sys import *
from os import *
from os.path import *
from urllib import *

release = argv[1]
dir = release.replace(".", "_")
mkdir(dir)

for classifier in [ "bin", "source-release" ]:
	for suffix in [ "zip", "zip.asc", "zip.md5"]:
		file = "axiom-" + release + "-" + classifier + "." + suffix
		urlretrieve("http://repository.apache.org/content/repositories/releases/org/apache/ws/commons/axiom/axiom/" + release + "/" + file, join(dir, file))

