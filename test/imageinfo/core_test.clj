(ns imageinfo.core-test
  (:require [clojure.test :refer :all]
            [imageinfo.core :refer :all]
            [clojure.java.io :as io]))

(def gif-files
  (->> (java.io.File. "test/imageinfo/test_images/gif")
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
      (for [file gif-files]
        (let [expected-dimensions (file->dimensions file)
              msg (str "expected: " file expected-dimensions)
              stream (io/input-stream file)
              size (read-gif stream)]
          (is (and (= (:width size) (first expected-dimensions)) 
                   (= (:height size) (second expected-dimensions)))))))))
                
