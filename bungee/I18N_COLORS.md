# I18n Colors Guide

The platform's i18n system is intended to be used with auto coloring feature based on the key names.
While even default configuration allows some freedom with naming conventions to provide better 
backwards compatible support for migrating developers, it is advised to stick to these rules:

- For error messages, key ending with `fail` or `error`: `config-load-fail`, `database-load-fail`, `permissions-error`
- For success messages, key ending with `success`: `config-load-success`, `player-add-success`

## colors.yml

All matchers are defined by `colors.yml` in the `path` of specific `@Messages` group. File would be unpacked from plugin's resources
or defaults and available to the end-user if `unpack = true`. When unpacking is disabled, it can be manually created.

Each matcher entry consists of:
- pattern: regex expression that must match whole key
- messageColor: color of the message
- fieldsColor: color of the dynamic fields (e.g. `{player.name}`) or null to disable

Keys are transformed to lower case before testing against patterns, mote the `.?` in `not.?found` 
that allows to support `notfound`, `not-found`, `not_found` with one expression.

Message color is applied by prepending messageColor to the message.
Field color is applied by prepending fieldsColor before field definition and appending messageColor at the end.

```yaml
# This file allows to define coloring pattern
# for all the messages that do not have defined colors.
#  
# Rules are checked from the top to the bottom.
# First rule matching key name would be applied.
#  
# Rule defines regex pattern, color for the message
# itself and the color for dynamic fields e.g. {xyz}.
#  
# Available colors:
# &4 - DARK_RED
# &c - RED
# &6 - GOLD
# &e - YELLOW
# &2 - DARK_GREEN
# &a - GREEN
# &b - AQUA
# &3 - DARK_AQUA
# &1 - DARK_BLUE
# &9 - BLUE
# &d - LIGHT_PURPLE
# &5 - DARK_PURPLE
# &f - WHITE
# &8 - DARK_GRAY
# &0 - BLACK
matchers:
- pattern: ^.*(fail|rejected|not.?found|limit.?reached|already|cannot|invalid|error).*$
  messageColor: RED
  fieldsColor: null
- pattern: ^.*(success|succeeded|accepted|created|removed|added|done).*$
  messageColor: GREEN
  fieldsColor: null
- pattern: ^.*$
  messageColor: YELLOW
  fieldsColor: null
```
