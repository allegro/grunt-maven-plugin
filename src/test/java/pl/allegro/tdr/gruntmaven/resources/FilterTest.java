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

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Adam Dubiel
 */
public class FilterTest {

    @Test
    public void shouldFilterOutPlaceholderInBrackets() {
        // given
        Filter filter = new Filter("placeholder", "Sparta!");
        String text = "This is ${placeholder}";

        // when
        String filteredText = filter.filter(text);

        // then
        assertThat(filteredText).isEqualTo("This is Sparta!");
    }

    @Test
    public void shouldReplaceAllOccurencesOfPlaceholder() {
        // given
        Filter filter = new Filter("placeholder", "Sparta!");
        String text = "This is ${placeholder}\n"
                + "Nice place, this ${placeholder}";

        // when
        String filteredText = filter.filter(text);

        // then
        assertThat(filteredText).isEqualTo("This is Sparta!\n"
                + "Nice place, this Sparta!");
    }

    @Test
    public void shouldNotReplaceUnbracketedPlaceholder() {
    // given
        Filter filter = new Filter("placeholder", "Sparta!");
        String text = "This is placeholder";

        // when
        String filteredText = filter.filter(text);

        // then
        assertThat(filteredText).isEqualTo("This is placeholder");
    }
}
