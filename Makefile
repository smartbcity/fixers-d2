libs: package-libs

package-libs:
	VERSION=${VERSION} ./gradlew clean build publishToMavenLocal publish
