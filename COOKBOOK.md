# CalendarIQ Cook Book

This is like a small how to guide, but less relevant.

## Performing a Release

Do this to release a new release:

* [ ] Update `VERSIONS.md` with a small text which summarizes the most important changes.
* [ ] Check that `README.md` is still sufficiently up-to-date.
* [ ] Update screenshots, if necessary.
* [ ] Create a new release branch `releases/<version>`.
* [ ] Update version code in build file on release branch. Be sure to clear the version suffix.
* [ ] Produce a build.
* [ ] Upload the build to the Play Store, using the text from the first step as release notes.
* [ ] Install new release and be sure that it works.
* [ ] Promote new release to production
* [ ] Do a GitHub release.
* [ ] Back on the `master` branch, bump the version number to match the current release, but increase the revision number.
* [ ] Add a new milestone on GitHub.
* [ ] Close the released milestone.
