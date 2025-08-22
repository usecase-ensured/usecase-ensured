# Usecase Ensured

A tool to bridge the gap between exploratory API testing and structured testing.

## Releasing a new version

All actions are performed in the `usecase-ensured` directory.

Run the command
`mvn deploy -s settings.xml -DUNAME="$USERNAME" -DPWD="$PASSWORD" -Dgpg.passphrase="$PASSPHRASE"`

How the above environment variables are provided is a separate topic,
it is important that their values do not show up in the bash history.

Instead of a global `settings.xml` a local `settings.xml` is used
in combination with the `UNAME`and`PWD`variables.
This way the values can be stored securely in a password manager like 1Password
and accessed programmatically on demand.

The `gpg.passphrase` is used in order to sign the artifacts to be publish on
<https://central.sonatype.com>

## Installing the library locally during development

Run the command
`mvn install -Dgpg.passphrase=$PASSPHRASE`. The gpg keys are expected to be in
your local gpg keychain. Currently looking for a better solution.
