(ns tweedler.security
  (:require [clojure.java.io :as io])
  (:import [org.owasp.html HtmlPolicyBuilder PolicyFactory]))

(def allowed-elements (doto ^"[Ljava.lang.String;" (make-array String 1)
                        (aset 0 "a")))

;; https://javadoc.io/static/com.googlecode.owasp-java-html-sanitizer/owasp-java-html-sanitizer/20191001.1/org/owasp/html/HtmlPolicyBuilder.html
(def policy-factory (-> (HtmlPolicyBuilder.)
                        ;; https://github.com/OWASP/java-html-sanitizer/blob/2ca428433034a349761eff03e84bbd9b763b4dcc/src/main/java/org/owasp/html/HtmlPolicyBuilder.java#L506
                        (.allowStandardUrlProtocols)
                        (.allowElements allowed-elements)
                        ;; https://github.com/OWASP/java-html-sanitizer/blob/2ca428433034a349761eff03e84bbd9b763b4dcc/src/main/java/org/owasp/html/HtmlPolicyBuilder.java#L252
                        (.allowCommonInlineFormattingElements)
                        ;; https://github.com/OWASP/java-html-sanitizer/blob/2ca428433034a349761eff03e84bbd9b763b4dcc/src/main/java/org/owasp/html/HtmlPolicyBuilder.java#L261
                        (.allowCommonBlockElements)
                        ;; Allow title= "..." on any element.
                        (.allowAttributes (doto ^"[Ljava.lang.String;" (make-array String 1) (aset 0 "title")))
                        (.globally)
                        ;; Allow href= "..." on <a> elements.
                        (.allowAttributes (doto ^"[Ljava.lang.String;" (make-array String 1) (aset 0 "href")))
                        (.onElements (doto ^"[Ljava.lang.String;" (make-array String 1) (aset 0 "a")))
                        (.allowStyling)
                        (.toFactory)))

(defn escape-html
  "Sanitizes the user's input to mitigate XSS attacks."
  [^String s]
  (.sanitize ^PolicyFactory policy-factory s))

(comment
  (def input-html (slurp (io/resource "templates/test-sanitize.html")))
  (def sanitized-html (escape-html input-html))

  (assert (.contains input-html "<script"))
  (assert (not (.contains sanitized-html "<script")))
  )
