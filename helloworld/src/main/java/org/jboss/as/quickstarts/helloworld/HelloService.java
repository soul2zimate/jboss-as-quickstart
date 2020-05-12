/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.helloworld;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

/**
 * A simple CDI service which is able to say hello to someone
 *
 * @author Pete Muir
 *
 */
public class HelloService {
    private static final boolean devProfile = System.getProperty("prbz_dev") != null;

    String createHelloMessage(String name) throws IOException {
        boolean devProfileEnv = System.getenv().get("prbz_dev") != null;
        String aphroditeConfigLocationEnv = System.getenv().get("aphrodite.config");
        // String aphroditeConfigLocationProperty = System.getProperty("aphrodite.config");
        String content = new String(Files.readAllBytes(Paths.get(aphroditeConfigLocationEnv)));

        String cacheDirEnv = System.getenv().get("cacheDir");
        String cacheNameEnv = System.getenv().get("cacheName");
        String cacheSizeEnv = System.getenv().get("cacheSize");

        File cacheDir =  new File(cacheDirEnv, cacheNameEnv);

        File f = new File(cacheDir, new Date().toString());
        f.createNewFile();

        String cacheInfo = cacheDirEnv + cacheNameEnv + cacheSizeEnv;

        return "Hello " + name + "!" + devProfile + devProfileEnv + "\n aphrodite config location from env "
                + aphroditeConfigLocationEnv + "\n cacheInfo " + cacheInfo + "\n content " + content;
    }

}