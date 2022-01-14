# appa
Appa is a test runner for `clojure.test` which allow you to annotate tests with
`^:parallel` metadata to run the specified test in a
ThreadPool in parallel or in a dedicated ThreadPool by itself.

Tests not specified are run sequentially in a single dedicated ThreadPool.

See `demo/appa-demo` for the complete usage example.

Your test cases will become

``` clojure
(ns appa-demo.core-test
  (:require [appa-demo.core :as sut]
            [clojure.test :refer [deftest is]]))

(deftest ^:parallel test-1
  (is (nil? (sut/print-and-sleep 1))))

(deftest ^:parallel test-2
  (is (true? (sut/print-and-sleep 2))))

(deftest test-3
  (is (nil? (sut/print-and-sleep 3))))

(deftest ^:dedicated test-4
  (is (nil? (sut/print-and-sleep 4))))
```

And then you can call the test suite from the command line using:

``` clojure
clj -M:test --parallelism true
```

and the output

``` shell

Running tests in #{"test"}

Running tests in parallel. Found 2 vars

Running tests in dedicated thread pool. Found 1 vars

Running tests... Found 1 vars
= start = Business logic number  = start = Business logic number  = start = Business logic number = start = Business logic number   3
1
4
2
= end = Business logic updated!
= end = Business logic updated!

FAIL in () (core_test.clj:9)
expected: (true? (sut/print-and-sleep 2))
  actual: (not (true? nil))
= end = Business logic updated!
= end = Business logic updated!

Ran 4 tests containing 4 assertions.
1 failures, 0 errors.
{:test 4, :pass 3, :fail 1, :error 0}
```

or run it sequentially which turns this library into [test-runner](https://github.com/cognitect-labs/test-runner).

``` clojure
clj -M:test --parallelism false
```

``` shell
Running tests in #{"test"}

Testing appa-demo.core-test
= start = Business logic number  3
= end = Business logic updated!
= start = Business logic number  1
= end = Business logic updated!
= start = Business logic number  2
= end = Business logic updated!

FAIL in (test-2) (core_test.clj:9)
expected: (true? (sut/print-and-sleep 2))
  actual: (not (true? nil))
= start = Business logic number  4
= end = Business logic updated!

Ran 4 tests containing 4 assertions.
1 failures, 0 errors.
{:test 4, :pass 3, :fail 1, :error 0, :type :summary}
```


# Prior attempts

[parallel-test](https://github.com/aredington/parallel-test): Using `core.async` and `robert.hook` library.


# License

Where noted, contains code from Clojure under license:

```
Copyright (c) Rich Hickey. All rights reserved.
The use and distribution terms for this software are covered by the
Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
which can be found in the file epl-v10.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.
You must not remove this notice, or any other, from this software.
```

Otherwise:

```
Copyright © 2022 Wanderson Ferreira

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
```

