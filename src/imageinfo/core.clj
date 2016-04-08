(ns imageinfo.core
  (:require [clojure.java.io :as io]))

;; ===== UTIL

;; TODO I forgot this was lazy...
(defn read-bytes [stream n]
  (for [x (range n)]
    (.read stream)))

(defn square [x]
  (* x x))

(defn u16 
  "returns a little endian unsigned 16 bit integer"
  [a b]
  (bit-or
    (bit-shift-left b 8)
    a))

(defn u32 [byte-values]
  (.getInt
    (doto (java.nio.ByteBuffer/wrap (byte-array byte-values))
      (.order java.nio.ByteOrder/BIG_ENDIAN))))

(def png-signature 
  [137 80 78 71 13 10 26 10])

(defn recognize-png [])

(defn recognize-jpeg [])

(defn recognize-gif [])

;; ===== PNG

(defn read-png [stream]
  ;; skip header info
  (doall (read-bytes stream 16))
  {:width (u32 (read-bytes stream 4))
   :height (u32 (read-bytes stream 4))})

;; ===== JPEG

(def jpeg-sof
  #{0xc0
    0xc1
    0xc2
    0xc3 
    0xc5 
    0xc6 
    0xc7 
    0xc9 
    0xca 
    0xcb 
    0xcd 
    0xce 
    0xcf}) 

(defn read-jpg [stream]
  (let [magick (read-bytes stream 2)]
    (loop [x stream]
      (let [byte (.read x)]
        (if (= -1 byte)
          false ;; done reading
          (if (= 0xff byte)
            (do 
              ;; if we hit marker, skip marker
              (read-bytes x 1)
              (recur x))
            (do
              (if (contains? jpeg-sof byte)
                (do (let [frame (read-bytes x 10)
                          w (u16 (nth frame 6) (nth frame 5))
                          h (u16 (nth frame 4) (nth frame 3))]
                      {:width w
                       :height h}))
                (recur x)))))))))

;; ===== GIFS 

(def gif-signature '(0x47 0x49 0x46))

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
    
    
    

