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
package cloud.commandframework.paper;

import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.BukkitBrigadierMapper;
import cloud.commandframework.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.permission.CommandPermission;
import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import java.lang.reflect.Method;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("deprecation") // Draft API
class PaperBrigadierListener<C> implements Listener {
    private static final @Nullable Method SET_RAW;

    static {
        @Nullable Method mth;
        try {
            mth = com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent.class
                    .getDeclaredMethod("setRawCommand", boolean.class);
        } catch (final NoSuchMethodException ex) {
            mth = null;
        }
        SET_RAW = mth;
    }

    private final CloudBrigadierManager<C, BukkitBrigadierCommandSource> brigadierManager;
    private final PaperCommandManager<C> paperCommandManager;

    PaperBrigadierListener(final @NonNull PaperCommandManager<C> paperCommandManager) {
        this.paperCommandManager = paperCommandManager;
        this.brigadierManager = new CloudBrigadierManager<>(this.paperCommandManager, () -> new CommandContext<>(
                this.paperCommandManager.getCommandSenderMapper().apply(Bukkit.getConsoleSender()),
                this.paperCommandManager
        ));

        this.brigadierManager.brigadierSenderMapper(sender ->
                this.paperCommandManager.getCommandSenderMapper().apply(sender.getBukkitSender()));

        new PaperBrigadierMapper<>(new BukkitBrigadierMapper<>(this.paperCommandManager, this.brigadierManager));

        this.brigadierManager
                .backwardsBrigadierSenderMapper(new BukkitBackwardsBrigadierSenderMapper<>(this.paperCommandManager));
    }

    protected @NonNull CloudBrigadierManager<C, BukkitBrigadierCommandSource> brigadierManager() {
        return this.brigadierManager;
    }

    @EventHandler
    public void onCommandRegister(
            final com.destroystokyo.paper.event.brigadier.
            @NonNull CommandRegisteredEvent<BukkitBrigadierCommandSource> event
    ) {
        if (!(event.getCommand() instanceof PluginIdentifiableCommand)) {
            return;
        } else if (!((PluginIdentifiableCommand) event.getCommand())
                .getPlugin().equals(this.paperCommandManager.getOwningPlugin())) {
            return;
        }

        final CommandTree<C> commandTree = this.paperCommandManager.commandTree();

        final String label;
        if (event.getCommandLabel().contains(":")) {
            label = event.getCommandLabel().split(Pattern.quote(":"))[1];
        } else {
            label = event.getCommandLabel();
        }

        final CommandTree.Node<CommandArgument<C, ?>> node = commandTree.getNamedNode(label);
        if (node == null) {
            return;
        }

        final BiPredicate<BukkitBrigadierCommandSource, CommandPermission> permissionChecker = (s, p) -> {
            // We need to check that the command still exists...
            if (commandTree.getNamedNode(label) == null) {
                return false;
            }

            final C sender = this.paperCommandManager.getCommandSenderMapper().apply(s.getBukkitSender());
            return this.paperCommandManager.hasPermission(sender, p);
        };
        event.setLiteral(this.brigadierManager.createLiteralCommandNode(
                node,
                event.getLiteral(),
                event.getBrigadierCommand(),
                event.getBrigadierCommand(),
                permissionChecker
        ));
        if (SET_RAW != null) {
            try {
                SET_RAW.invoke(event, true);
            } catch (final ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
