(ns imageinfo.core-test
  (:require [clojure.test :refer :all]
            [imageinfo.core :refer :all]
            [clojure.java.io :as io]))

;; ===== GIF

(defn files [path]
  (->> (java.io.File. path)
       (file-seq)
       (filter #(.isFile %))))

(defn file->dimensions [file]
  (-> (.getName file)
      (clojure.string/split #"\.")
      (first)
      (clojure.string/split #"x")
      (->> (map read-string))))

(deftest gif-dimensions
  (testing "getting the correct dimensions from a gif canvas"
    (doall
      (for [file (files "test/imageinfo/test_images/gif")]
        (let [expected-dimensions (file->dimensions file)
              ; msg (str "expected: " file expected-dimensions)
              stream (io/input-stream file)
              size (read-gif stream)]
          (is (and (= (:width size) (first expected-dimensions)) 
                   (= (:height size) (second expected-dimensions)))))))))
                
;; ===== JEPG

(deftest jpeg-dimensions
  (testing "jpeg dimensions" 
    (doall
      (for [file (files "test/imageinfo/test_images/jpg")]
        (let [[exp-width exp-height] (file->dimensions file)
               stream (io/input-stream file)
               size (read-jpg stream)]
          (is (and (= (:width size) exp-width)
                   (= (:height size) exp-height))))))))

;; ===== PNG

(deftest png-dimensions
  (testing "png dimensions" 
    (doall
      (for [file (files "test/imageinfo/test_images/png")]
        (let [[exp-width exp-height] (file->dimensions file)
               stream (io/input-stream file)
               size (read-png stream)]
          (is (= (:width size) exp-width)))))))

