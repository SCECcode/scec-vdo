package org.scec.vtk.tools;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.opensha.commons.param.editor.impl.FileParameterEditor;
import org.opensha.commons.param.impl.FileParameter;

/** A file chooser that remembers successful loads' directories, without restoring the file value. */
public class RememberedFileParameter extends FileParameter {
	private static final long serialVersionUID = 1L;
	private final transient Preferences preferences;
	private final String preferenceKey;
	private File rememberedDirectory;
	private transient FileParameterEditor rememberedEditor;

	public RememberedFileParameter(String name, Class<?> preferenceOwner, String preferenceKey) {
		this(name, preferencesFor(() -> Preferences.userNodeForPackage(preferenceOwner)), preferenceKey);
	}

	/** Uses an explicit preference node so a package rename can preserve the remembered directory. */
	public static RememberedFileParameter forPreferenceNode(String name, String nodePath, String preferenceKey) {
		return new RememberedFileParameter(name, preferencesFor(() -> Preferences.userRoot().node(nodePath)),
				preferenceKey);
	}

	RememberedFileParameter(String name, Preferences preferences, String preferenceKey) {
		super(name);
		this.preferences = preferences;
		this.preferenceKey = preferenceKey;
		if (preferences != null) {
			try {
				String saved = preferences.get(preferenceKey, null);
				if (saved != null && !saved.isBlank()) rememberedDirectory = new File(saved);
			} catch (SecurityException | IllegalStateException e) {
				System.err.println("Could not restore file chooser directory: " + e.getMessage());
			}
		}
	}

	private static Preferences preferencesFor(Supplier<Preferences> location) {
		try {
			return location.get();
		} catch (SecurityException e) {
			System.err.println("File chooser preferences unavailable: " + e.getMessage());
			return null;
		}
	}

	@Override
	public File getDefaultInitialDir() {
		File directory = existingDirectory(rememberedDirectory);
		return directory == null ? existingDirectory(super.getDefaultInitialDir()) : directory;
	}

	/** A deleted folder falls back to its nearest readable ancestor; null lets Swing choose its usual default. */
	private static File existingDirectory(File directory) {
		try {
			if (directory != null) directory = directory.getAbsoluteFile();
			while (directory != null) {
				if (directory.isDirectory() && directory.canRead()) return directory;
				directory = directory.getParentFile();
			}
		} catch (SecurityException e) {
			// Try the configured fallback instead of preventing the dialog from opening.
		}
		return null;
	}

	/** Call after a successful load, not merely after selecting a potentially invalid file. */
	public void rememberLoadedFile(File file) {
		if (file != null) rememberDirectory(file.getAbsoluteFile().getParentFile());
	}

	public void rememberDirectory(File directory) {
		if (directory == null) return;
		File existing = existingDirectory(directory);
		if (existing == null) return;
		rememberedDirectory = existing;
		if (preferences != null) {
			try {
				preferences.put(preferenceKey, existing.getAbsolutePath());
				preferences.flush();
			} catch (BackingStoreException | SecurityException | IllegalStateException | IllegalArgumentException e) {
				// Keep the in-memory directory even if persistence is unavailable.
				System.err.println("Could not save file chooser directory: " + e.getMessage());
			}
		}
	}

	@Override
	public FileParameterEditor getEditor() {
		if (rememberedEditor == null) {
			rememberedEditor = new FileParameterEditor(this) {
				@Override public void actionPerformed(ActionEvent event) {
					// Recheck on every opening: a folder can disappear while VDO is running.
					setDefaultDir(RememberedFileParameter.this.getDefaultInitialDir());
					super.actionPerformed(event);
				}
			};
		}
		return rememberedEditor;
	}

	@Override
	public boolean isEditorBuilt() {
		return rememberedEditor != null;
	}
}
