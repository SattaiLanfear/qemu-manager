import AssemblyKeys._ // put this at the top of the file

assemblySettings

// your assembly settings here

jarName in assembly := "qemu.jar"

test in assembly := {}

mainClass in assembly := Some("com.greyscribes.IptablesManager")

