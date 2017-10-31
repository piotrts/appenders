(ns piotrts.appenders
  (:require [taoensso.timbre :as timbre]
            [taoensso.encore :as enc]
            [clojure.java.shell :as shell]
            [clojure.string :as string])
  (:import (java.io IOException File)
           (java.text SimpleDateFormat)))

;; based on taoensso.timbre.appenders.core/spit-appender
(defn datetime-spit-appender
  "Returns a datetime-spit appender for Clojure. fname-format
  is a java.text.SimpleDateFormat pattern."
  [& [{:keys [fname-format append?]
        :or  {fname-format  "'./timbre-spit-'yyyy-MM-dd'.log'"
              append? true}}]]
  (let [date-format (SimpleDateFormat. fname-format)]
    {:enabled?   true
     :async?     false
     :min-level  nil
     :rate-limit nil
     :output-fn  :inherit
     :fn         (fn self [data]
                   (let [{:keys [output_ instant]} data
                         fname (.format date-format instant)]
                     (try
                       (spit fname (str (force output_) "\n") :append append?)
                       (catch IOException e
                         (if (:__spit-appender/retry? data)
                           (throw e) ; Unexpected error
                           (let [_ (enc/have? enc/nblank-str? fname)
                                 file (File. ^String fname)
                                 dir (.getParentFile (.getCanonicalFile file))]
                             (when-not (.exists dir) (.mkdirs dir))
                             (self (assoc data :__spit-appender/retry? true))))))))}))

(def level->urgency-default
  {:debug :low
   :info  :low
   :warn  :normal
   :error :critical})

(def level->timeout-default {})

(defn notify-appender
  "Notify appender for Clojure. Displays notifications using `notify-send`."
  [& [{:keys [level->urgency level->timeout]
       :or   {level->urgency level->urgency-default
              level->timeout level->timeout-default}}]]
  {:enabled?   true
   :async?     false
   :min-level  nil
   :rate-limit nil
   :output-fn  (fn [data]
                 (let [{:keys [level ?err msg_]} data]
                   {:summary (str "<b>" (-> level name string/upper-case) "</b>: " (force msg_))
                    :body    (if ?err
                               (timbre/stacktrace ?err {:stacktrace-fonts {}})
                               "")}))
   :fn         (fn [data]
                 (let [{:keys [level output_]} data
                       {:keys [summary body]} (force output_)]
                   (shell/sh "notify-send"
                             "-a" "timbre"
                             "-u" (-> (level->urgency level :normal) name str)
                             "-t" (str (level->timeout level -1))
                             summary body)))})
