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
package cloud.commandframework.internal;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.StaticArgument;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Utility that registers commands natively for whatever
 * platform the library is used in. This can do nothing, if
 * the target platform does not have its own concept of commands
 */
@FunctionalInterface
@API(status = API.Status.STABLE)
public interface CommandRegistrationHandler {

    /**
     * Create a new {@link CommandRegistrationHandler} that does nothing
     *
     * @return Constructed registration
     */
    static @NonNull CommandRegistrationHandler nullCommandRegistrationHandler() {
        return new NullCommandRegistrationHandler();
    }

    /**
     * Attempt to register the command
     *
     * @param command Command to register
     * @return {@code true} if the command was registered successfully,
     *         else {@code false}
     */
    boolean registerCommand(@NonNull Command<?> command);

    /**
     * Requests that the given {@code rootCommand} should be unregistered.
     *
     * @param rootCommand The command to delete
     * @since 1.7.0
     */
    default void unregisterRootCommand(final @NonNull StaticArgument<?> rootCommand) {
    }


    @API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.*")
    final class NullCommandRegistrationHandler implements CommandRegistrationHandler {

        private NullCommandRegistrationHandler() {
        }

        @Override
        public boolean registerCommand(final @NonNull Command<?> command) {
            return true;
        }

        @Override
        public void unregisterRootCommand(final @NonNull StaticArgument<?> rootCommand) {
        }
    }
}
