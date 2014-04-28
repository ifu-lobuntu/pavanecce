package org.pavanecce.common.text.workspace;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.pavanecce.common.util.VersionNumber;

public class TextProject extends TextOutputNode{
	Set<SourceFolder> sourceFolders = new HashSet<SourceFolder>();
	static final SourceFolderDefinition NON_MODIFIABLE_SOURCE_FOLDER_DEFINITION = new SourceFolderDefinition(SourceFolderNameStrategy.MODEL_NAME, "", false){
		{
			dontCleanDirectoriesOrOverwriteFiles();
		}
	};

	private boolean isRegenerated=true;
	public TextProject(TextWorkspace textWorkspace,String name){
		super(name);
		super.parent = textWorkspace;
	}
	public SourceFolder findOrCreateSourceFolder(SourceFolderDefinition def, String baseName, VersionNumber vn){
		String name=def.generateSourceFolderName(baseName,vn);
		SourceFolder result = findOrCreateSourceFolder(name, def);
		return result;
	}
	private SourceFolder findOrCreateSourceFolder(String name, SourceFolderDefinition def) {
		for(SourceFolder r:sourceFolders){
			if(r.name.equals(name)){
				return r;
			}
		}
		SourceFolder result = new SourceFolder(def, this, name);
		sourceFolders.add(result);
		return result;
	}
	public Collection<SourceFolder> getSourceFolders(){
		return sourceFolders;
	}
	@Override
	public boolean hasContent(){
		return sourceFolders.size() > 0;
	}
	public SourceFolder mapExistingSourceFolder(String name){
		SourceFolder result = findOrCreateSourceFolder(name, NON_MODIFIABLE_SOURCE_FOLDER_DEFINITION);
		result.setRegenerated(false);
		return result;
	}

	public boolean isRegenerated(){
		return isRegenerated;
	}
	public void setRegenerated(boolean b){
		this.isRegenerated=b;
	}

}
