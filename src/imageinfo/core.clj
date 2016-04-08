(ns imageinfo.core
  (:require [clojure.java.io :as io]))

;; ===== UTIL

(defn read-bytes [stream n]
  (for [x (range n)]
    (.read stream)))

(defn square [x]
  (* x x))

(def png-signature 
  [137 80 78 71 13 10 26 10])

(defn recognize-png [])

(defn recognize-jpeg [])

(defn recognize-gif [])

;; ===== GIFS 

(def gif-signature '(0x47 0x49 0x46))

(defn u16 
  "returns a little endian unsigned 16 bit integer"
  [a b]
  (bit-or
    (bit-shift-left b 8)
    a))

;; close stream??
(defn read-gif [stream]
  (let [header (read-bytes stream 6)
        is-gif? (= gif-signature (take 3 header))
        screen-descriptor (read-bytes stream 7)]
    {:width (u16 (first screen-descriptor) (second screen-descriptor))
     :height (u16 (nth screen-descriptor 2) (nth screen-descriptor 3))}))

; (defn read-gif-canvas-width [screen-descriptor]
;   (println "GETTING WIDT")
;   (bit-or 
;     (nth screen-descriptor 1)
;     (bit-shift-left (nth screen-descriptor 0) 8)))

; (defn read-gif-canvas-height [screen-descriptor]
;   (bit-or 
;     (bit-shift-left (nth screen-descriptor 2) 8)
;     (nth screen-descriptor 3)))

; (defn get-color-table-size [screen-descriptor]
;   (let [val (nth screen-descriptor 4)
;         binary-string (Integer/toBinaryString val)
;         ;; the size of the color table is the last 3 bits of the packed field
;         color-table-size-value (subs binary-string 5 8)
;         color-table-n (Integer/parseInt color-table-size-value, 2)]
;     (println "packed field ->" binary-string)
;     (println "color size int" color-table-n)
;     (square (+ color-table-n 1))))

; (defn read-image-descriptor [slice]
;   (println
;     "WIDTH IN DESC 1"
;     (u16 (nth slice 4)
;          (nth slice 5))))

    ; (println (u16 (first screen-descriptor) (second screen-descriptor)))
    ; (println (read-gif-canvas-width screen-descriptor))))
    ; (println "canvas width: " (read-gif-canvas-width screen-descriptor))
    ; (println "canvas height: " (read-gif-canvas-height screen-descriptor))
    ; (println "color table size" (get-color-table-size screen-descriptor))))

    ; (let [color-table-size (* 3 (get-color-table-size screen-descriptor))]
    ;   (println "skipping color table size of" color-table-size)
    ;   (read-bytes stream color-table-size)
    ;   (println (read-bytes stream 2)))))
        
      ; (loop [x stream]
      ;   (let [byte (.read x)]
      ;     (if (= 0x3b byte)
      ;       (println "END OF FILE")
      ;       (if (= 0x2c byte)
      ;         (do (read-image-descriptor (read-bytes x 9)))
      ;         (recur x))))))))

      ; (loop [x stream]
      ;   (println (read-bytes x 100))
      ;   (recur x)))))

      ; (loop [x stream]
      ;   (let [byte (.read x)]
      ;     (if (= 0x3b byte)
      ;       (println "END OF FILE")
      ;       (if (= 0x2c byte)
      ;         (do (read-image-descriptor (read-bytes stream 9)))
      ;         (recur stream))))))))

      ; (while (not= 0x3b (.read stream))
      ;   (if (= 0x2c (.read stream))
      ;     (println "YO"))))))
    
    
    

