(ns tweedler.utils
  "Miscellaneous utilities.")

(defmacro docstring
  "Returns the doctring (if any) of a symbol."
  [symbol]
  `(:doc (meta #'~symbol)))
