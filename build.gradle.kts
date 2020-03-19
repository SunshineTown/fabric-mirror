plugins {
    id ("fabric-loom") version "0.2.6-SNAPSHOT"
}

version = properties["mod_version"] as String + "-" + properties["minecraft_version"]
group = properties["maven_group"] as String
extra["archivesBaseName"] =  properties["archives_base_name"]

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")
    modCompile("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modCompile(	"net.fabricmc.fabric-api:fabric-commands-v0:0.1.2+28f8190f42")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.getByName<ProcessResources>("processResources"){
    inputs.property("version", project.version)

    from(sourceSets.main.get().resources.srcDirs) {
        include("fabric.mod.json")
        expand("version" to project.version)
    }

    from(sourceSets.main.get().resources.srcDirs) {
        exclude("fabric.mod.json")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}