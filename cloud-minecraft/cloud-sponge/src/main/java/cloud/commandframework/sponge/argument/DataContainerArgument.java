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
package cloud.commandframework.sponge.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.sponge.NodeSupplyingArgumentParser;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.nbt.CompoundTag;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.common.data.persistence.NBTTranslator;

/**
 * Argument for parsing {@link DataContainer DataContainers} from
 * <a href="https://minecraft.fandom.com/wiki/NBT_format">SNBT</a> strings.
 *
 * @param <C> sender type
 */
public final class DataContainerArgument<C> extends CommandArgument<C, DataContainer> {

    private DataContainerArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull String defaultValue,
            final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(
                required,
                name,
                new Parser<>(),
                defaultValue,
                DataContainer.class,
                suggestionsProvider,
                defaultDescription
        );
    }

    /**
     * Create a new required {@link DataContainerArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return a new {@link DataContainerArgument}
     */
    public static <C> @NonNull DataContainerArgument<C> of(final @NonNull String name) {
        return DataContainerArgument.<C>builder(name).build();
    }

    /**
     * Create a new optional {@link DataContainerArgument}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return a new {@link DataContainerArgument}
     */
    public static <C> @NonNull DataContainerArgument<C> optional(final @NonNull String name) {
        return DataContainerArgument.<C>builder(name).asOptional().build();
    }

    /**
     * Create a new optional {@link DataContainerArgument} with the specified default value.
     *
     * @param name         argument name
     * @param defaultValue default value
     * @param <C>          sender type
     * @return a new {@link DataContainerArgument}
     */
    public static <C> @NonNull DataContainerArgument<C> optional(
            final @NonNull String name,
            final @NonNull DataContainer defaultValue
    ) {
        return DataContainerArgument.<C>builder(name).asOptionalWithDefault(defaultValue).build();
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return a new {@link Builder}
     */
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Parser for {@link DataContainer DataContainers} from SNBT.
     *
     * @param <C> sender type
     */
    public static final class Parser<C> implements NodeSupplyingArgumentParser<C, DataContainer> {

        private final ArgumentParser<C, DataContainer> mappedParser =
                new WrappedBrigadierParser<C, CompoundTag>(CompoundTagArgument.compoundTag())
                        .map((ctx, compoundTag) ->
                                ArgumentParseResult.success(NBTTranslator.INSTANCE.translate(compoundTag)));

        @Override
        public @NonNull ArgumentParseResult<@NonNull DataContainer> parse(
                @NonNull final CommandContext<@NonNull C> commandContext,
                @NonNull final Queue<@NonNull String> inputQueue
        ) {
            return this.mappedParser.parse(commandContext, inputQueue);
        }

        @Override
        public CommandTreeNode.@NonNull Argument<? extends CommandTreeNode.Argument<?>> node() {
            return CommandTreeNodeTypes.NBT_COMPOUND_TAG.get().createNode();
        }

    }

    /**
     * Builder for {@link DataContainerArgument}.
     *
     * @param <C> sender type
     */
    public static final class Builder<C> extends TypedBuilder<C, DataContainer, Builder<C>> {

        Builder(final @NonNull String name) {
            super(DataContainer.class, name);
        }

        @Override
        public @NonNull DataContainerArgument<C> build() {
            return new DataContainerArgument<>(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }

        /**
         * Sets the command argument to be optional, with the specified default value.
         *
         * @param defaultValue default value
         * @return this builder
         * @see CommandArgument.Builder#asOptionalWithDefault(String)
         */
        public @NonNull Builder<C> asOptionalWithDefault(final @NonNull DataContainer defaultValue) {
            return this.asOptionalWithDefault(NBTTranslator.INSTANCE.translate(defaultValue).toString());
        }

    }

}
