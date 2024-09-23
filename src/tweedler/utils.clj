(ns tweedler.utils
  "This namespace contains some utility functions.")

(defmacro docstring
  "Return the doctring (if any) of a symbol."
  [symbol]
  `(:doc (meta #'~symbol)))
