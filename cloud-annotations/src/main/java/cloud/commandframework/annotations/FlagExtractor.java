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
package cloud.commandframework.annotations;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserRegistry;
import cloud.commandframework.permission.Permission;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

final class FlagExtractor implements Function<@NonNull Method, Collection<@NonNull CommandFlag<?>>> {

    private final CommandManager<?> commandManager;
    private final AnnotationParser<?> annotationParser;

    FlagExtractor(
            final @NonNull CommandManager<?> commandManager,
            final @NonNull AnnotationParser<?> annotationParser
    ) {
        this.commandManager = commandManager;
        this.annotationParser = annotationParser;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NonNull Collection<@NonNull CommandFlag<?>> apply(final @NonNull Method method) {
        final Collection<CommandFlag<?>> flags = new LinkedList<>();
        for (final Parameter parameter : method.getParameters()) {
            if (!parameter.isAnnotationPresent(Flag.class)) {
                continue;
            }
            final Flag flag = parameter.getAnnotation(Flag.class);
            final String flagName = this.annotationParser.processString(flag.value());

            CommandFlag.Builder<Void> builder = this.commandManager
                    .flagBuilder(this.annotationParser.processString(flagName))
                    .withDescription(ArgumentDescription.of(this.annotationParser.processString(flag.description())))
                    .withAliases(this.annotationParser.processStrings(flag.aliases()))
                    .withPermission(Permission.of(this.annotationParser.processString(flag.permission())));
            if (flag.repeatable()) {
                builder = builder.asRepeatable();
            }

            if (parameter.getType().equals(boolean.class)) {
                flags.add(builder.build());
            } else {
                final TypeToken<?> token;
                if (flag.repeatable() && Collection.class.isAssignableFrom(parameter.getType())) {
                    token = TypeToken.get(GenericTypeReflector.getTypeParameter(
                            parameter.getParameterizedType(),
                            Collection.class.getTypeParameters()[0]
                    ));
                } else {
                    token = TypeToken.get(parameter.getType());
                }

                if (token.equals(TypeToken.get(boolean.class))) {
                    flags.add(builder.build());
                    continue;
                }

                final Collection<Annotation> annotations = Arrays.asList(parameter.getAnnotations());
                final ParserRegistry<?> registry = this.commandManager.parserRegistry();
                final ArgumentParser<?, ?> parser;
                final String parserName = this.annotationParser.processString(flag.parserName());
                if (parserName.isEmpty()) {
                    parser = registry.createParser(token, registry.parseAnnotations(token, annotations))
                            .orElse(null);
                } else {
                    parser = registry.createParser(parserName, registry.parseAnnotations(token, annotations))
                            .orElse(null);
                }
                if (parser == null) {
                    throw new IllegalArgumentException(
                            String.format("Cannot find parser for type '%s' for flag '%s' in method '%s'",
                                    parameter.getType().getCanonicalName(), flagName, method.getName()
                            ));
                }
                final BiFunction<?, @NonNull String, @NonNull List<String>> suggestionProvider;
                final String suggestions = this.annotationParser.processString(flag.suggestions());
                if (!suggestions.isEmpty()) {
                    suggestionProvider = registry.getSuggestionProvider(suggestions).orElse(null);
                } else {
                    suggestionProvider = null;
                }
                final CommandArgument.Builder argumentBuilder0 = CommandArgument.ofType(
                        parameter.getType(),
                        flagName
                );
                final CommandArgument.Builder argumentBuilder = argumentBuilder0.asRequired()
                        .manager(this.commandManager)
                        .withParser(parser);
                final CommandArgument argument;
                if (suggestionProvider != null) {
                    argument = argumentBuilder.withSuggestionsProvider(suggestionProvider).build();
                } else {
                    argument = argumentBuilder.build();
                }
                flags.add(builder.withArgument(argument).build());
            }
        }
        return flags;
    }
}
