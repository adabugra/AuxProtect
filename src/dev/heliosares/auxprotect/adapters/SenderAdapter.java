package dev.heliosares.auxprotect.adapters;

import java.util.UUID;

import dev.heliosares.auxprotect.core.Language;
import dev.heliosares.auxprotect.core.PlatformType;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class SenderAdapter {
	public abstract Object getSender();

	public abstract String getName();

	public abstract UUID getUniqueId();

	public abstract PlatformType getPlatform();

	public void sendLang(String namespace, Object... format) {
		Language.send(this, namespace, format);
	}

	public abstract void sendMessage(BaseComponent... message);

	public abstract void sendMessageRaw(String message);

	public abstract boolean hasPermission(String node);

	public abstract void executeCommand(String command);

	public abstract boolean isConsole();

	public abstract void teleport(String world, int x, int y, int z, int pitch, int yaw)
			throws NullPointerException, UnsupportedOperationException;
}