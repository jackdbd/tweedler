(ns tweedler.utils
  "This namespace contains some utility functions."
  (:import [org.owasp.html HtmlPolicyBuilder]))

(def allowed-elements (doto (make-array String 1)
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
                        (.allowAttributes (doto (make-array String 1) (aset 0 "title")))
                        (.globally)
                        ;; Allow href= "..." on <a> elements.
                        (.allowAttributes (doto (make-array String 1) (aset 0 "href")))
                        (.onElements (doto (make-array String 1) (aset 0 "a")))
                        (.allowStyling)
                        (.toFactory)))

(defn escape-html
  "Sanitize the user's input to mitigate XSS attacks."
  [^String s]
  (.sanitize policy-factory s))
