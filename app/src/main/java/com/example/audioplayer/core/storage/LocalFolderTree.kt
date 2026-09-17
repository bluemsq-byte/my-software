package com.example.audioplayer.core.storage

import com.example.audioplayer.core.model.AudioTrack

data class LocalFolderNode(
    val path: String,
    val name: String,
    val childFolders: List<LocalFolderNode>,
    val tracks: List<AudioTrack>,
)

data class LocalFolderCrumb(
    val path: String,
    val name: String,
)

object LocalFolderTree {
    fun build(tracks: List<AudioTrack>): LocalFolderNode {
        val folderPaths = tracks.map { track -> folderPath(track) }
        val commonSegments = commonSegments(folderPaths)
        val rootPath = pathOf(commonSegments)
        val root = MutableFolderNode(
            path = rootPath,
            name = displayName(rootPath),
        )

        tracks.forEach { track ->
            val relativeSegments = segments(folderPath(track)).drop(commonSegments.size)
            var node = root
            relativeSegments.forEach { segment ->
                node = node.children.getOrPut(segment) {
                    MutableFolderNode(
                        path = appendPath(node.path, segment),
                        name = segment,
                    )
                }
            }
            node.tracks += track
        }

        return root.freeze()
    }

    fun findNode(root: LocalFolderNode, path: String): LocalFolderNode? {
        if (root.path == path) return root
        return root.childFolders.firstNotNullOfOrNull { child ->
            findNode(child, path)
        }
    }

    fun breadcrumbs(root: LocalFolderNode, path: String): List<LocalFolderCrumb> {
        val result = mutableListOf<LocalFolderCrumb>()
        fun visit(node: LocalFolderNode): Boolean {
            result += LocalFolderCrumb(node.path, node.name)
            if (node.path == path) return true
            node.childFolders.forEach { child ->
                if (visit(child)) return true
            }
            result.removeAt(result.lastIndex)
            return false
        }
        visit(root)
        return result
    }

    private fun commonSegments(folderPaths: List<String>): List<String> {
        if (folderPaths.isEmpty()) return emptyList()
        var common = segments(folderPaths.first())
        folderPaths.drop(1).forEach { path ->
            val candidate = segments(path)
            var index = 0
            while (
                index < common.size &&
                index < candidate.size &&
                common[index].equals(candidate[index], ignoreCase = true)
            ) {
                index++
            }
            common = common.take(index)
        }
        return common
    }

    private fun folderPath(track: AudioTrack): String =
        normalize(track.remotePath.orEmpty().ifBlank { "/" })

    private fun normalize(path: String): String {
        val normalized = path.replace('\\', '/')
        return if (normalized.startsWith('/')) normalized.trimEnd('/').ifBlank { "/" } else {
            "/$normalized".trimEnd('/').ifBlank { "/" }
        }
    }

    private fun segments(path: String): List<String> =
        normalize(path).split('/').filter(String::isNotBlank)

    private fun pathOf(segments: List<String>): String =
        if (segments.isEmpty()) "/" else "/${segments.joinToString("/")}"

    private fun appendPath(parent: String, segment: String): String =
        if (parent == "/") "/$segment" else "$parent/$segment"

    private fun displayName(path: String): String =
        segments(path).lastOrNull() ?: "内部存储"

    private data class MutableFolderNode(
        val path: String,
        val name: String,
        val children: LinkedHashMap<String, MutableFolderNode> = linkedMapOf(),
        val tracks: MutableList<AudioTrack> = mutableListOf(),
    ) {
        fun freeze(): LocalFolderNode = LocalFolderNode(
            path = path,
            name = name,
            childFolders = children.values
                .map(MutableFolderNode::freeze)
                .sortedBy { it.name.lowercase() },
            tracks = tracks.sortedBy { it.title.lowercase() },
        )
    }
}
