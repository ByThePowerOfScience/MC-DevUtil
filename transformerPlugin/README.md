Hooks into the Architectury plugin to allow two things:
1. Defining custom transformers outside of the `common {}` block
2. Creating a transformed variant of the common module WITHOUT running Arch's "prod" transformers that'll break the dev runs.