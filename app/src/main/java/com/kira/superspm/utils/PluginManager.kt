package com.kira.superspm.utils

import android.content.Context
import android.util.Log
import com.kira.superspm.data.model.PluginConfig
import com.kira.superspm.R
import com.kira.superspm.data.model.PluginType
import com.kira.superspm.plugin.BasePlugin
import com.kira.superspm.plugin.HistoryService
import com.kira.superspm.plugin.Plugin
import com.kira.superspm.plugin.impl.HistoryServiceImpl
import dalvik.system.DexClassLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.util.zip.ZipInputStream

object PluginManager {
    private const val TAG = "PluginManager"
    private const val PLUGIN_DIR = "plugins"
    private const val CONFIG_FILE = "plugin.json"
    private const val STATE_FILE = "plugin_states.json"
    private const val DEX_DIR = "dex"

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    private val _plugins = MutableStateFlow<List<InstalledPlugin>>(emptyList())
    val plugins: StateFlow<List<InstalledPlugin>> = _plugins

    data class InstalledPlugin(
        val config: PluginConfig,
        val dirName: String,
        val enabled: Boolean,
        val processor: Plugin? = null
    )

    @Serializable
    data class PluginState(
        val dirName: String,
        val enabled: Boolean
    )

    fun init(context: Context) {
        BasePlugin.setHistoryService(HistoryServiceImpl)
        loadPlugins(context)
    }

    private fun getPluginDir(context: Context): File {
        val dir = File(context.filesDir, PLUGIN_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getDexDir(context: Context): File {
        val dir = File(context.filesDir, DEX_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun loadPlugins(context: Context) {
        val states = loadStates(context)
        val list = mutableListOf<InstalledPlugin>()
        val pluginDir = getPluginDir(context)

        pluginDir.listFiles()?.forEach { dir ->
            if (dir.isDirectory) {
                val configFile = File(dir, CONFIG_FILE)
                if (configFile.exists()) {
                    try {
                        val config = json.decodeFromString<PluginConfig>(configFile.readText())
                        val state = states.find { it.dirName == dir.name }
                        val enabled = state?.enabled ?: true
                        var processor: Plugin? = null

                        if (config.type == PluginType.NATIVE && enabled) {
                            processor = loadNativePlugin(context, dir)
                        }

                        list.add(InstalledPlugin(config, dir.name, enabled, processor))
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load plugin: ${dir.name}", e)
                    }
                }
            }
        }

        _plugins.value = list
    }

    private fun loadNativePlugin(context: Context, pluginDir: File): Plugin? {
        return try {
            val sourceDexFile = findDexFile(pluginDir) ?: return null
            val optimizedDir = File(getDexDir(context), pluginDir.name)
            optimizedDir.mkdirs()

            val targetDexFile = File(optimizedDir, "classes.dex")
            if (!targetDexFile.exists() || targetDexFile.lastModified() < sourceDexFile.lastModified()) {
                Files.copy(sourceDexFile.toPath(), targetDexFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                targetDexFile.setReadOnly()
            }

            val classLoader = DexClassLoader(
                targetDexFile.absolutePath,
                optimizedDir.absolutePath,
                null,
                context.classLoader
            )

            val configFile = File(pluginDir, CONFIG_FILE)
            val config = json.decodeFromString<PluginConfig>(configFile.readText())

            val entryClass = if (config.entry.contains(".")) {
                config.entry
            } else {
                "com.kira.superspm.plugin.${config.entry}"
            }

            val clazz = classLoader.loadClass(entryClass)
            val instance = clazz.getDeclaredConstructor().newInstance()

            if (instance is Plugin) {
                instance
            } else {
                Log.e(TAG, "Plugin main class does not implement Plugin interface: $entryClass")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load Native plugin", e)
            null
        }
    }

    private fun findDexFile(dir: File): File? {
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.endsWith(".dex")) {
                return file
            }
        }
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                val found = findDexFile(file)
                if (found != null) return found
            }
        }
        return null
    }

    fun importPlugin(context: Context, inputStream: InputStream, fileName: String): Result<InstalledPlugin> {
        return try {
            val pluginDir = getPluginDir(context)
            val baseName = fileName.substringBeforeLast(".")
            var targetDir = File(pluginDir, baseName)
            var counter = 1
            while (targetDir.exists()) {
                targetDir = File(pluginDir, "${baseName}_$counter")
                counter++
            }
            targetDir.mkdirs()

            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val outFile = File(targetDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        outFile.outputStream().use { os ->
                            zis.copyTo(os)
                        }
                    }
                    entry = zis.nextEntry
                }
            }

            val configFile = File(targetDir, CONFIG_FILE)
            if (!configFile.exists()) {
                targetDir.deleteRecursively()
                return Result.failure(Exception(context.getString(R.string.plugin_config_not_found)))
            }

            val config = json.decodeFromString<PluginConfig>(configFile.readText())

            var processor: Plugin? = null
            if (config.type == PluginType.NATIVE) {
                processor = loadNativePlugin(context, targetDir)
                if (processor == null) {
                    Log.w(TAG, context.getString(R.string.native_plugin_load_failed_warning))
                }
            }

            val plugin = InstalledPlugin(config, targetDir.name, true, processor)
            val currentList = _plugins.value.toMutableList()
            currentList.add(plugin)
            _plugins.value = currentList
            saveStates(context)

            Result.success(plugin)
        } catch (e: Exception) {
            Log.e(TAG, context.getString(R.string.import_plugin_failed), e)
            Result.failure(e)
        }
    }

    fun setPluginEnabled(context: Context, dirName: String, enabled: Boolean) {
        val currentList = _plugins.value.toMutableList()
        val index = currentList.indexOfFirst { it.dirName == dirName }
        if (index >= 0) {
            val plugin = currentList[index]
            var newProcessor = plugin.processor

            if (enabled && plugin.processor == null && plugin.config.type == PluginType.NATIVE) {
                val pluginDir = File(getPluginDir(context), dirName)
                newProcessor = loadNativePlugin(context, pluginDir)
            }

            newProcessor?.setEnabled(enabled)
            currentList[index] = plugin.copy(enabled = enabled, processor = newProcessor)
            _plugins.value = currentList
            saveStates(context)
        }
    }

    fun deletePlugin(context: Context, dirName: String) {
        val pluginDir = getPluginDir(context)
        val targetDir = File(pluginDir, dirName)
        if (targetDir.exists()) {
            targetDir.deleteRecursively()
        }
        val dexDir = File(getDexDir(context), dirName)
        if (dexDir.exists()) {
            dexDir.deleteRecursively()
        }
        val currentList = _plugins.value.toMutableList()
        currentList.removeAll { it.dirName == dirName }
        _plugins.value = currentList
        saveStates(context)
    }

    fun getPluginDir(context: Context, dirName: String): File? {
        val pluginDir = getPluginDir(context)
        val targetDir = File(pluginDir, dirName)
        return if (targetDir.exists()) targetDir else null
    }

    fun getProcessor(dirName: String): Plugin? {
        return _plugins.value.find { it.dirName == dirName && it.enabled }?.processor
    }

    fun getHistoryService(): HistoryService {
        return HistoryServiceImpl
    }

    private fun loadStates(context: Context): List<PluginState> {
        val stateFile = File(context.filesDir, STATE_FILE)
        if (!stateFile.exists()) return emptyList()
        return try {
            val statesJson = stateFile.readText()
            json.decodeFromString(ListSerializer(PluginState.serializer()), statesJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveStates(context: Context) {
        val states = _plugins.value.map { PluginState(it.dirName, it.enabled) }
        val stateFile = File(context.filesDir, STATE_FILE)
        stateFile.writeText(json.encodeToString(ListSerializer(PluginState.serializer()), states))
    }
}