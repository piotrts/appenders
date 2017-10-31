# Appenders for Timbre

Currently contains:
 - datetime-spit-appender
 - notify-appender

## Usage
```clojure
(require '[piotrts.appenders :as appenders])

(timbre/merge-config!
  {;; ...
   :appenders {;; for datetime-spit-appender
               :dt-spit (appenders/datetime-spit-appender)
               ;; for notify-appender
               :notify (appenders/notify-appender)
               ;; ...
              }})
```

# Additional notes

## datetime-spit-appender

To specify output path (that should be a *java.text.SimpleDateFormat* pattern) pass a map containing `:fname-format` param to `datetime-spit-appender` function.

## notify-appender

Make sure have `notify-send` in your `$PATH` for this to work.

Contributors welcome!

