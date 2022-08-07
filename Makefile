libs: package-libs

package-libs:
	VERSION=${VERSION} gradle clean build publishToMavenLocal publish
