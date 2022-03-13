package shaders;

import processing.core.PGraphics;
import toolbox.global.NodeTree;
import toolbox.windows.nodes.FolderNode;

public class GeneralPurposeShaders {

    public static void applyShaders(String path, PGraphics pg){
        PremadeShaderFolder node = (PremadeShaderFolder) NodeTree.findNodeByPathInTree(path);
        if (node == null) {
            FolderNode parentFolder = (FolderNode) NodeTree.getLazyInitParentFolderByPath(path);
            node = new PremadeShaderFolder(path, parentFolder);
            NodeTree.insertNodeAtItsPath(node);
        }
        node.applyShaders(pg);
    }

    public static void applyFilters(String path, PGraphics pg) {
        PremadeFilterFolder node = (PremadeFilterFolder) NodeTree.findNodeByPathInTree(path);
        if (node == null) {
            FolderNode parentFolder = (FolderNode) NodeTree.getLazyInitParentFolderByPath(path);
            node = new PremadeFilterFolder(path, parentFolder);
            NodeTree.insertNodeAtItsPath(node);
        }
        node.applyFilters(pg);
    }
}
