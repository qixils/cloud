//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package cloud.commandframework.arguments.standard;

import cloud.commandframework.TestCommandSender;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

@ExtendWith(MockitoExtension.class)
class IntegerParserTest {

    @Mock
    private CommandContext<TestCommandSender> context;

    @Test
    void Parse_NoMinMax_SuccessfulParse() {
        // Arrange
        final IntegerArgument.IntegerParser<TestCommandSender> parser = new IntegerArgument.IntegerParser<>(
                IntegerArgument.IntegerParser.DEFAULT_MINIMUM,
                IntegerArgument.IntegerParser.DEFAULT_MAXIMUM
        );

        final int intInput = ThreadLocalRandom.current().nextInt();
        final LinkedList<String> input = ArgumentTestHelper.linkedListOf(Integer.toString(intInput));

        // Act
        final ArgumentParseResult<Integer> result = parser.parse(
                this.context,
                input
        );

        // Assert
        assertThat(result.getFailure()).isEmpty();
        assertThat(result.getParsedValue()).hasValue(intInput);

        assertThat(input).isEmpty();
    }

    @Test
    void Parse_ValueBelowMin_FailedParse() {
        // Arrange
        final IntegerArgument.IntegerParser<TestCommandSender> parser = new IntegerArgument.IntegerParser<>(
                5 /* min */,
                IntegerArgument.IntegerParser.DEFAULT_MAXIMUM
        );

        // Act
        final ArgumentParseResult<Integer> result = parser.parse(
                this.context,
                ArgumentTestHelper.linkedListOf("4")
        );

        // Assert
        assertThat(result.getFailure()).hasValue(new IntegerArgument.IntegerParseException(
                "4",
                parser,
                this.context
        ));
        assertThat(result.getParsedValue()).isEmpty();
    }

    @Test
    void Parse_ValueAboveMax_FailedParse() {
        // Arrange
        final IntegerArgument.IntegerParser<TestCommandSender> parser = new IntegerArgument.IntegerParser<>(
                IntegerArgument.IntegerParser.DEFAULT_MINIMUM,
                5 /* max */
        );

        // Act
        final ArgumentParseResult<Integer> result = parser.parse(
                this.context,
                ArgumentTestHelper.linkedListOf("6")
        );

        // Assert
        assertThat(result.getFailure()).hasValue(new IntegerArgument.IntegerParseException(
                "6",
                parser,
                this.context
        ));
        assertThat(result.getParsedValue()).isEmpty();
    }

    @Test
    void Parse_NonIntegerInput_FailedParse() {
        // Arrange
        final IntegerArgument.IntegerParser<TestCommandSender> parser = new IntegerArgument.IntegerParser<>(
                IntegerArgument.IntegerParser.DEFAULT_MINIMUM,
                IntegerArgument.IntegerParser.DEFAULT_MAXIMUM
        );

        // Act
        final ArgumentParseResult<Integer> result = parser.parse(
                this.context,
                ArgumentTestHelper.linkedListOf("cow")
        );

        // Assert
        assertThat(result.getFailure()).hasValue(new IntegerArgument.IntegerParseException(
                "cow",
                parser,
                this.context
        ));
        assertThat(result.getParsedValue()).isEmpty();
    }

    @Test
    void Suggestions_EmptyInput_ExpectedSuggestions() {
        // Arrange
        final IntegerArgument.IntegerParser<TestCommandSender> parser = new IntegerArgument.IntegerParser<>(
                IntegerArgument.IntegerParser.DEFAULT_MINIMUM,
                IntegerArgument.IntegerParser.DEFAULT_MAXIMUM
        );

        final List<String> expectedSuggestions = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            expectedSuggestions.add(Integer.toString(i));
        }

        // Act
        final List<String> suggestions = parser.suggestions(
                this.context,
                ""
        );

        // Assert
        assertThat(suggestions).containsExactlyElementsIn(expectedSuggestions);
    }

    @Test
    void Suggestions_NegativeSignInput_ExpectedSuggestions() {
        // Arrange
        final IntegerArgument.IntegerParser<TestCommandSender> parser = new IntegerArgument.IntegerParser<>(
                IntegerArgument.IntegerParser.DEFAULT_MINIMUM,
                IntegerArgument.IntegerParser.DEFAULT_MAXIMUM
        );

        final List<String> expectedSuggestions = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            expectedSuggestions.add(Integer.toString(-i));
        }

        // Act
        final List<String> suggestions = parser.suggestions(
                this.context,
                "-"
        );

        // Assert
        assertThat(suggestions).containsExactlyElementsIn(expectedSuggestions);
    }
}
