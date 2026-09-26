// asphodel::merge -> loader=*, version=1.20.1 -> com.example.asphodel.Asphodel20
// asphodel::merge -> loader=*, version=1.21.1 -> com.example.asphodel.Asphodel21
package com.example.asphodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Asphodel {

    private static final int EXAMPLE = 26;

    /*
     * Welcome to the Asphodel Gradle Template!
     * here is a crash-course on what all of the source-sets mean and how to get started using this environment for mod development
     * this here is the asphodel/src where code that is shared for all mod loaders and versions reside.
     *
     * When using this source-set be very careful to follow these rules closely:
     * - You must ONLY call artifacts from specifically 1.20.1 and utilize just Java 17 features in this source-set.
     * - Avoid using modloader specific code, stick to only Minecraft or common libraries that you are certain haven't changed much between verisons.
     * - When you need to call code from newer versions or specific modloaders use the Asphodel Gradle directive comments.
     * Failure to respect the above guidelines may lead to code not compiling on certain versions or loaders; here be dragons!
     *
     * One of the greatest tools you have to mediate work between versions and loaders is the Asphodel Gradle directive comments
     * This very file contains an example of these directive comments, which you can find above the package.
     *
     * asphodel::merge merges code from another class into this one at build time.
     * The general syntax is:
     *      // asphodel::merge -> loader=<loader>, version=<version> -> <fully.qualified.ClassName>
     * loader may be "fabric", "forge", "neoforge", or "*" to match any loader.
     * version may be "1.20.1", "1.21.1", or "*" to match any version.
     *
     * The class name on the right must be the fully qualified name of a top-level class that lives in any-
     * source-set which is compiled for the current loader and version.
     *
     * Every member (method, field, constructor, nested type, initializer) of the referenced class is copied
     * into this class after the merge. A member here with a matching signature is replaced by the contributed
     * member; a member here with no counterpart is left alone; a contributed member with no counterpart is
     * added. If the same member key appears twice with different signatures the build fails with a clear
     * error instead of producing invalid Java.
     *
     * For example, this file declares two stubs:
     *      public static void setup20() {} // stub
     *      public static void setup21() {} // stub
     *
     * On a 1.20.1 build the Asphodel20 contribution replaces setup20 with the real implementation and leaves
     * setup21 as a no-op stub. On a 1.21.1 build the opposite happens. The result is a single class that
     * does the right thing on every platform without any runtime dispatch.
     *
     * You can stack as many directives as you want above the package. Each one that matches the current
     * loader and version contributes its members in the order the directives appear. When two matching
     * directives contribute the same member the later one wins, so order them from most general to most
     * specific.
     *
     * Any member of a contributor can be opted out of the merge with an exclude directive placed directly above it, like so:
     *      // asphodel::exclude
     *      public static int sharedField = 0;
     * The excluded member is not copied into the primary class and this is useful when the contributor needs-
     * to reference a member that already exists in the primary; the contributor declares a local copy so its own code compiles standalone.
     *
     * The source-sets you can pull from are:
     *   asphodel/src/main/java              shared by every loader and every version
     *   <version>/common/src/main/java      shared by all loaders of one version
     *   <version>/<loader>/src/main/java    specific to one loader and one version
     *
     * Code in the asphodel source-set must compile against 1.20.1 and Java 17, because it is also compiled
     * into the 1.20.1 artifacts. Code in a version-specific source-set may use the Java language level and
     * Minecraft APIs of that version. Code in a loader-specific source-set may also use that loader's API.
     *
     * If you need to call a version-specific or loader-specific method from shared code, declare it here as
     * a stub and provide the real implementation in the appropriate contribution class.
     */

    public static final String MOD_ID = "asphodel";
    public static final Logger LOGGER = LoggerFactory.getLogger("Example Mod");

    public static void sharedSetup() {
    }

    public static void setup20() {} // stub
    public static void setup21() {} // stub
}