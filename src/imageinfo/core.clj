(ns imageinfo.core
  (:require [clojure.java.io :as io]))

;; ===== UTIL

;; TODO I forgot this was lazy...
(defn- read-bytes [stream n]
  (doall
    (for [x (range n)]
      (.read stream))))

(defn- square [x]
  (* x x))

(defn- u16-le
  "returns a little endian unsigned 16 bit integer"
  [a b]
  (bit-or
    (bit-shift-left b 8)
    a))

(defn- u16-be
  [byte-values]
  (.getInt
    (doto (java.nio.ByteBuffer/wrap (byte-array (concat [0 0] byte-values)))
      (.order java.nio.ByteOrder/BIG_ENDIAN))))

(defn- u32 [byte-values]
  (.getInt
    (doto (java.nio.ByteBuffer/wrap (byte-array byte-values))
      (.order java.nio.ByteOrder/BIG_ENDIAN))))

;; ===== PNG

(defn- read-png [first-six-bytes stream]
  ;; skip header info
  (read-bytes stream 10)
  {:width (u32 (read-bytes stream 4))
   :height (u32 (read-bytes stream 4))
   :type :png})

;; ===== JPEG

(def jpeg-sof
  {0xc0 true
   0xc1 true
   0xc2 true
   0xc3 true
   0xc5 true
   0xc6 true
   0xc7 true
   0xc9 true
   0xca true
   0xcb true
   0xcd true
   0xce true
   0xcf true}) 

(defn- read-jpg [first-six-bytes stream]
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
              ;; if non-sof jump to next marker
              (if (nil? (get jpeg-sof byte))
                (do 
                  (let []
                      ;; this advance is required
                      (read-bytes x 2)
                      ;; TODO needs to skip to the next marker here
                      ;; otherwise there's the possibility of reading an 
                      ;; SOF marker that is actually image data
                      (recur x)))
                (do 
                    ;; advance into to data frame
                    (read-bytes x 3)
                    {:height (u16-be (read-bytes x 2))
                     :width (u16-be (read-bytes x 2)) 
                     :type :jpg})))))))))

;; ===== GIFS 

;; close stream??
(defn- read-gif [first-six-bytes stream]
  (let [header first-six-bytes
        screen-descriptor (read-bytes stream 7)]
    {:width (u16-le (first screen-descriptor) (second screen-descriptor))
     :height (u16-le (nth screen-descriptor 2) (nth screen-descriptor 3))
     :type :gif}))

;; ===== INFO

(def jpg-signature
  '(0xff 0xd8))

(def gif-signature 
  '(0x47 0x49 0x46))

(def png-signature 
  '(0x89 0x50 0x4e 0x47 0x0D 0x0A))

;; This impl works and passes all tests but kinda sucks
;; I really want/need an immutable stream representation that doesn't
;; deplete on .read calls. Hence the first-six-bytes bullshit
(defn info [stream]
  (let [first-six-bytes (doall (read-bytes stream 6))]
    (cond
      (= jpg-signature (take 2 first-six-bytes)) (read-jpg first-six-bytes stream)
      (= gif-signature (take 3 first-six-bytes)) (read-gif first-six-bytes stream)
      (= png-signature first-six-bytes) (read-png first-six-bytes stream)
      :else nil)))

