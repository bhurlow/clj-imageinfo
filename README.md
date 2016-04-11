# imageinfo

extract image type and size from an inputstream

## Installation

add to your project dependencies
```
[imageinfo "0.0.2"]
```

## Usage

currently exposes only 1 function `info`:

```
(use 'imageinfo)

(info (io/input-stream "test/imageinfo/test_images/png/42x165.png"))

=> {:width 42, :height 165, :type :png}
```

## License

Copyright © 2016 Brian Hurlow

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
