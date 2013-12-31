/*
 * Copyright 2013 Adam Dubiel, Przemek Hertel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.allegro.tdr.gruntmaven.resources;

/**
 *
 * @author Adam Dubiel
 */
class Filter {

    private final String placeholderFormat = "\\$\\{%s\\}";

    private final String placeholder;

    private final String value;

    Filter(String placeholder, String value) {
        this.placeholder = String.format(placeholderFormat, placeholder);
        this.value = value.replaceAll("\\\\", "/");
    }

    String filter(String text) {
        return text.replaceAll(placeholder, value);
    }
}
