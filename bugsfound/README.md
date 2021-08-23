# Bugs found in tested algorithms

## [SnapTreeMap](../src/SnapTree/SnapTreeMap.java)

[Lincheck](https://github.com/Kotlin/kotlinx-lincheck) found error while executing following scenario:
```
Execution scenario (init part):
[putIfAbsent(2, 4), putIfAbsent(4, 2)]
Execution scenario (parallel part):
| putIfAbsent(6, 4) | lastKey() |
| remove(4)         |           |
```

The following interleaving leads to the error:
```
Parallel part trace:
|                      | lastKey()                                                                                                           |
|                      |   lastKey(): threw AssertionError at IntIntSnapTreeMapTest.lastKey(IntIntSnapTreeMapTest.kt:44)                     |
|                      |     extremeKeyOrThrow(R): threw AssertionError at SnapTreeMap.lastKey(SnapTreeMap.java:645)                         |
|                      |       extreme(true,R): threw AssertionError at SnapTreeMap.extremeKeyOrThrow(SnapTreeMap.java:654)                  |
|                      |         holderRef.READ: COWMgr@1 at SnapTreeMap.extreme(SnapTreeMap.java:666)                                       |
|                      |         read(): RootHolder@1 at SnapTreeMap.extreme(SnapTreeMap.java:666)                                           |
|                      |         right.READ: Node@1 at SnapTreeMap.extreme(SnapTreeMap.java:666)                                             |
|                      |         shrinkOVL.READ: 0 at SnapTreeMap.extreme(SnapTreeMap.java:670)                                              |
|                      |         holderRef.READ: COWMgr@1 at SnapTreeMap.extreme(SnapTreeMap.java:674)                                       |
|                      |         read(): RootHolder@1 at SnapTreeMap.extreme(SnapTreeMap.java:674)                                           |
|                      |         right.READ: Node@1 at SnapTreeMap.extreme(SnapTreeMap.java:674)                                             |
|                      |         attemptExtreme(true,R,Node@1,0): threw AssertionError at SnapTreeMap.extreme(SnapTreeMap.java:676)          |
|                      |           child(R): Node@2 at SnapTreeMap.attemptExtreme(SnapTreeMap.java:691)                                      |
|                      |           shrinkOVL.READ: 0 at SnapTreeMap.attemptExtreme(SnapTreeMap.java:708)                                     |
|                      |           child(R): Node@2 at SnapTreeMap.attemptExtreme(SnapTreeMap.java:716)                                      |
|                      |           shrinkOVL.READ: 0 at SnapTreeMap.attemptExtreme(SnapTreeMap.java:723)                                     |
|                      |           attemptExtreme(true,R,Node@2,0): threw AssertionError at SnapTreeMap.attemptExtreme(SnapTreeMap.java:727) |
|                      |             child(R): null at SnapTreeMap.attemptExtreme(SnapTreeMap.java:691)                                      |
|                      |             switch                                                                                                  |
| putIfAbsent(6, 4)    |                                                                                                                     |
| remove(4)            |                                                                                                                     |
|   thread is finished |                                                                                                                     |
|                      |             vOpt.READ: null at SnapTreeMap.attemptExtreme(SnapTreeMap.java:697)                                     |
|                      |             shrinkOVL.READ: 0 at SnapTreeMap.attemptExtreme(SnapTreeMap.java:699)                                   |
```

We can see that one of asserts fails:
```
java.lang.AssertionError
	at SnapTree.SnapTreeMap.attemptExtreme(SnapTreeMap.java:703)
```

Visualization of the error:
![alt text](IntIntSnapTreeMap/illustration/SnapTreeMapBug.png)

That is, it turns out that if there was no `remove(4)`, everything would be fine, because it would just turn out that `lastKey()` seemed to be executed earlier than `putIfAbsent(6,4)`. If there was no `putIfAbsent(6,4)`, there would also be no error, because the 4:2 node would have been cut out of the tree, but the value in it would not have changed. In our case, the node cannot be cut out of the tree due to the *partial external tree* approach, so it is simply marked as a routing node, which is why `assert` is triggered.

There is no mistake directly in the article, because it says:
>Readers ‘optimistically’ assume that no mutation will occur during a critical region, and then retry if that assumption fails.

Therefore, if we take this case into account in the implementation and make a check instead of `assert` and, if necessary, retry, then this error ceases to occur.

For more information check [IntIntSnapTreeMap](IntIntSnapTreeMap).

## [LogicalOrderingAVL](../src/LogicalOrderingAVL/LogicalOrderingAVL.java)

[Lincheck](https://github.com/Kotlin/kotlinx-lincheck) found deadlock while executing following scenario:
```
Execution scenario (init part):
[put(5, 3)]
Execution scenario (parallel part):
| putIfAbsent(3, 8) | put(1, 7) | remove(3) |
```

For interleaving and other information please check [IntIntLogicalOrderingAVL](IntIntLogicalOrderingAVL).

Here is a brief description of the reason for the resulting hang:

Initially, there are two nodes in the tree, with the keys MIN_VALUE and MAX_VALUE.
MAX_VALUE is the right child of MIN_VALUE, and MAX_VALUE is the root.
Then, during the init part, a node with the key 5 is added, it becomes the left child for MAX_VALUE.
The parallel part begins, the second thread comes, takes the locks it needs and finishes inserting the node with key 1 (it becomes the left child for 5), but does not yet release the locks, including the `treeLock` of the node with key 5, before switching.
There is a switch, and the first thread comes.
It inserts a node with the value 3 (it becomes the right child for 1), but to prepare for rebalancing the tree, it tries to take the `treeLock` of the node with the key 5.
Since the second thread holds it, it does not succeed, and the first thread starts waiting.
There is a switch, and a third thread comes, which wants to delete the node with the key 3.
To do this, it starts taking locks, including taking `treeLock` of the node with the key 3 and trying to take `treeLock` of the node with the key 1.
Since the first thread is holding it, it does not succeed, and the third thread also starts waiting.
Then the second thread releases all the locks and finishes working.
After that, the first thread successfully takes the `treeLock` of the node with the key 5, but then hangs in an infinite loop of attempts to take the `treeLock` of the node with the key 3, which is held by the third thread.
As a result, we have a deadlock, when the first and third threads are infinitely waiting for each other.

## [CATreeMapAVL](../src/CATreeMapAVL/CATreeMapAVL.java)

[Lincheck](https://github.com/Kotlin/kotlinx-lincheck) found deadlock while executing following scenario:
```
Execution scenario (parallel part):
| clear() | isEmpty() |
| get(7)  |           |
```

The following interleaving leads to the error:
```
Parallel part trace:
|                                                                                                           | isEmpty()                                                                                                  |
|                                                                                                           |   isEmpty(): true at IntIntCATreeMapAVLTest.isEmpty(IntIntCATreeMapAVLTest.kt:32)                          |
|                                                                                                           |     size(): 0 at CATreeMapAVL.isEmpty(CATreeMapAVL.java:539)                                               |
|                                                                                                           |       lockAll() at CATreeMapAVL.size(CATreeMapAVL.java:532)                                                |
|                                                                                                           |         root.READ: DualLFCASAVLTreeMapSTD@1 at CATreeMapAVL.lockAll(CATreeMapAVL.java:504)                 |
|                                                                                                           |         lockAllHelper(DualLFCASAVLTreeMapSTD@1,ArrayList@1) at CATreeMapAVL.lockAll(CATreeMapAVL.java:504) |
|                                                                                                           |           lock() at CATreeMapAVL.lockAllHelper(CATreeMapAVL.java:470)                                      |
|                                                                                                           |             tryLock(): false at DualLFCASAVLTreeMapSTD.lock(DualLFCASAVLTreeMapSTD.java:731)               |
|                                                                                                           |               tryLock(): false at DualLFCASAVLTreeMapSTD.tryLock(DualLFCASAVLTreeMapSTD.java:720)          |
|                                                                                                           |                 seqNumber.READ: 2 at SeqLock.tryLock(SeqLock.java:60)                                      |
|                                                                                                           |                 switch                                                                                     |
| clear()                                                                                                   |                                                                                                            |
| get(7)                                                                                                    |                                                                                                            |
|   switch                                                                                                  |                                                                                                            |
|                                                                                                           |                 seqNumber.compareAndSet(2,3): false at SeqLock.tryLock(SeqLock.java:64)                    |
|                                                                                                           |             lock() at DualLFCASAVLTreeMapSTD.lock(DualLFCASAVLTreeMapSTD.java:735)                         |
|                                                                                                           |             isLockFreeMode(): false at DualLFCASAVLTreeMapSTD.lock(DualLFCASAVLTreeMapSTD.java:736)        |
|                                                                                                           |             addToContentionStatistics() at DualLFCASAVLTreeMapSTD.lock(DualLFCASAVLTreeMapSTD.java:739)    |
|                                                                                                           |           isValid(): true at CATreeMapAVL.lockAllHelper(CATreeMapAVL.java:471)                             |
|                                                                                                           |       root.READ: DualLFCASAVLTreeMapSTD@1 at CATreeMapAVL.size(CATreeMapAVL.java:533)                      |
|                                                                                                           |       sizeHelper(DualLFCASAVLTreeMapSTD@1): 0 at CATreeMapAVL.size(CATreeMapAVL.java:533)                  |
|                                                                                                           |       unlockAll() at CATreeMapAVL.size(CATreeMapAVL.java:534)                                              |
|                                                                                                           |   thread is finished                                                                                       |
|   get(7) at IntIntCATreeMapAVLTest.get(IntIntCATreeMapAVLTest.kt:38)                                      |                                                                                                            |
|     getBaseNode(7): DualLFCASAVLTreeMapSTD@1 at CATreeMapAVL.get(CATreeMapAVL.java:550)                   |                                                                                                            |
|     getOptimisticReadToken(): 0 at CATreeMapAVL.get(CATreeMapAVL.java:552)                                |                                                                                                            |
|     validateOptimisticReadToken(0): false at CATreeMapAVL.get(CATreeMapAVL.java:569)                      |                                                                                                            |
|     lockIfNotLockFree() at CATreeMapAVL.get(CATreeMapAVL.java:573)                                        |                                                                                                            |
|       getLockFreeMap(): null at DualLFCASAVLTreeMapSTD.lockIfNotLockFree(DualLFCASAVLTreeMapSTD.java:781) |                                                                                                            |
|       tryLock(): false at DualLFCASAVLTreeMapSTD.lockIfNotLockFree(DualLFCASAVLTreeMapSTD.java:791)       |                                                                                                            |
|       lock() at DualLFCASAVLTreeMapSTD.lockIfNotLockFree(DualLFCASAVLTreeMapSTD.java:800)                 |                                                                                                            |
|         seqNumber.READ: 3 at SeqLock.lock(SeqLock.java:75)                                                |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:77)                                                      |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:78)                                                      |                                                                                                            |
|         seqNumber.READ: 3 at SeqLock.lock(SeqLock.java:79)                                                |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:77)                                                      |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:78)                                                      |                                                                                                            |
|         seqNumber.READ: 3 at SeqLock.lock(SeqLock.java:79)                                                |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:77)                                                      |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:78)                                                      |                                                                                                            |
|         seqNumber.READ: 3 at SeqLock.lock(SeqLock.java:79)                                                |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:77)                                                      |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:78)                                                      |                                                                                                            |
|         seqNumber.READ: 3 at SeqLock.lock(SeqLock.java:79)                                                |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:77)                                                      |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:78)                                                      |                                                                                                            |
|         seqNumber.READ: 3 at SeqLock.lock(SeqLock.java:79)                                                |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:77)                                                      |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:78)                                                      |                                                                                                            |
|         seqNumber.READ: 3 at SeqLock.lock(SeqLock.java:79)                                                |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:77)                                                      |                                                                                                            |
|         fullFence() at SeqLock.lock(SeqLock.java:78)                                                      |                                                                                                            |
|         seqNumber.READ: 3 at SeqLock.lock(SeqLock.java:79)                                                |                                                                                                            |
All threads are in deadlock
```

Nothing is stored in the tree during the entire test.
When creating the tree, `root` is initialized with an empty instance of `DualLFCASAVLTreeMapSTD`.
The `isEmpty()` method starts working in the second thread and calls the `size()` method.
It reads `root`, but doesn't take its lock before switch.
The first thread comes, changes `root` to a new node in `clear()`, then `clear()` finishes.
`get(7)` starts, but doesn't do anything before switch.
The second thread wakes up and takes the lock of the old `root`, which is already discarded.
Then the `size` value is calculated for the new `root` (it is unsafe, since the lock is taken on the old `root` value).
After that, the second thread tries to release the lock of the current `root`, which it did not take.
To release the lock, the `seqNumber = seqNumber + 1` operation is performed, which, according to the authors' idea, should change an odd value (that is, a locked lock) to an even value (that is, an unlocked lock).
But because of the error that occurred, exactly the opposite happens.
Since the second thread finishes after that, this lock is now permanently locked, which leads to a deadlock on the first attempt to take this lock.

## [ConcurrencyOptimalTreeMap](../src/ConcurrencyOptimalTreeMap/ConcurrencyOptimalTreeMap.java)

[Lincheck](https://github.com/Kotlin/kotlinx-lincheck) found error while executing following scenario:
```
Parallel part:
| putIfAbsent(5, 6): NullPointerException | putIfAbsent(6, 1): null |
```

The following interleaving leads to the error:
```
Parallel part trace:
|                                                                                                                                              | putIfAbsent(6, 1)                                                                                                                                |
|                                                                                                                                              |   putIfAbsent(6,1): null at IntIntConcurrencyOptimalTreeMapTest.putIfAbsent(IntIntConcurrencyOptimalTreeMapTest.kt:29)                           |
|                                                                                                                                              |     <init>(ConcurrencyOptimalTreeMap@1) at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:452)                             |
|                                                                                                                                              |       l.READ: null at ConcurrencyOptimalTreeMap$Window.<init>(ConcurrencyOptimalTreeMap.java:411)                                                |
|                                                                                                                                              |     traverse(6,Window@1): Window@1 at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:454)                                  |
|                                                                                                                                              |       comparable(6): 6 at ConcurrencyOptimalTreeMap.traverse(ConcurrencyOptimalTreeMap.java:432)                                                 |
|                                                                                                                                              |         comparator.READ: null at ConcurrencyOptimalTreeMap.comparable(ConcurrencyOptimalTreeMap.java:333)                                        |
|                                                                                                                                              |     validateRefAndTryLock(Node@1,null,true): true at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:478)                   |
|                                                                                                                                              |       tryWriteLockWithConditionRefLeft(null): true at ConcurrencyOptimalTreeMap.validateRefAndTryLock(ConcurrencyOptimalTreeMap.java:357)        |
|                                                                                                                                              |         l.READ: null at ConcurrencyOptimalTreeMap$Node.tryWriteLockWithConditionRefLeft(ConcurrencyOptimalTreeMap.java:165)                      |
|                                                                                                                                              |         lStamp.READ: 0 at ConcurrencyOptimalTreeMap$Node.tryWriteLockWithConditionRefLeft(ConcurrencyOptimalTreeMap.java:166)                    |
|                                                                                                                                              |         compareAndSetLeftStamp(0,1): true at ConcurrencyOptimalTreeMap$Node.tryWriteLockWithConditionRefLeft(ConcurrencyOptimalTreeMap.java:169) |
|                                                                                                                                              |           compareAndSwapInt(Node@1,16,0,1): true at ConcurrencyOptimalTreeMap$Node.compareAndSetLeftStamp(ConcurrencyOptimalTreeMap.java:68)     |
|                                                                                                                                              |         l.READ: null at ConcurrencyOptimalTreeMap$Node.tryWriteLockWithConditionRefLeft(ConcurrencyOptimalTreeMap.java:170)                      |
|                                                                                                                                              |       deleted.READ: false at ConcurrencyOptimalTreeMap.validateRefAndTryLock(ConcurrencyOptimalTreeMap.java:361)                                 |
|                                                                                                                                              |     readLockState() at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:479)                                                 |
|                                                                                                                                              |       stateStamp.READ: 0 at ConcurrencyOptimalTreeMap$Node.readLockState(ConcurrencyOptimalTreeMap.java:125)                                     |
|                                                                                                                                              |       compareAndSetStateStamp(0,2): true at ConcurrencyOptimalTreeMap$Node.readLockState(ConcurrencyOptimalTreeMap.java:129)                     |
|                                                                                                                                              |         compareAndSwapInt(Node@1,12,0,2): true at ConcurrencyOptimalTreeMap$Node.compareAndSetStateStamp(ConcurrencyOptimalTreeMap.java:64)      |
|                                                                                                                                              |     deleted.READ: false at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:480)                                             |
|                                                                                                                                              |     switch                                                                                                                                       |
| putIfAbsent(5, 6)                                                                                                                            |                                                                                                                                                  |
|   putIfAbsent(5,6): threw NullPointerException at IntIntConcurrencyOptimalTreeMapTest.putIfAbsent(IntIntConcurrencyOptimalTreeMapTest.kt:29) |                                                                                                                                                  |
|     <init>(ConcurrencyOptimalTreeMap@1) at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:452)                         |                                                                                                                                                  |
|       l.READ: null at ConcurrencyOptimalTreeMap$Window.<init>(ConcurrencyOptimalTreeMap.java:411)                                            |                                                                                                                                                  |
|     traverse(5,Window@2): Window@2 at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:454)                              |                                                                                                                                                  |
|       comparable(5): 5 at ConcurrencyOptimalTreeMap.traverse(ConcurrencyOptimalTreeMap.java:432)                                             |                                                                                                                                                  |
|         comparator.READ: null at ConcurrencyOptimalTreeMap.comparable(ConcurrencyOptimalTreeMap.java:333)                                    |                                                                                                                                                  |
|     validateRefAndTryLock(Node@1,null,true): false at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:478)              |                                                                                                                                                  |
|       tryWriteLockWithConditionRefLeft(null): false at ConcurrencyOptimalTreeMap.validateRefAndTryLock(ConcurrencyOptimalTreeMap.java:357)   |                                                                                                                                                  |
|         l.READ: null at ConcurrencyOptimalTreeMap$Node.tryWriteLockWithConditionRefLeft(ConcurrencyOptimalTreeMap.java:165)                  |                                                                                                                                                  |
|         lStamp.READ: 1 at ConcurrencyOptimalTreeMap$Node.tryWriteLockWithConditionRefLeft(ConcurrencyOptimalTreeMap.java:166)                |                                                                                                                                                  |
|       deleted.READ: false at ConcurrencyOptimalTreeMap.validateRefAndTryLock(ConcurrencyOptimalTreeMap.java:361)                             |                                                                                                                                                  |
|     deleted.READ: false at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:493)                                         |                                                                                                                                                  |
|     traverse(5,Window@2): threw NullPointerException at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:454)            |                                                                                                                                                  |
|       comparable(5): 5 at ConcurrencyOptimalTreeMap.traverse(ConcurrencyOptimalTreeMap.java:432)                                             |                                                                                                                                                  |
|         comparator.READ: null at ConcurrencyOptimalTreeMap.comparable(ConcurrencyOptimalTreeMap.java:333)                                    |                                                                                                                                                  |
|   result: NullPointerException                                                                                                               |                                                                                                                                                  |
|   thread is finished                                                                                                                         |                                                                                                                                                  |
|                                                                                                                                              |     l.WRITE(Node@2) at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:482)                                                 |
|                                                                                                                                              |     unlockReadState() at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:486)                                               |
|                                                                                                                                              |       stateStamp.READ: 2 at ConcurrencyOptimalTreeMap$Node.unlockReadState(ConcurrencyOptimalTreeMap.java:288)                                   |
|                                                                                                                                              |       compareAndSetStateStamp(2,0): true at ConcurrencyOptimalTreeMap$Node.unlockReadState(ConcurrencyOptimalTreeMap.java:289)                   |
|                                                                                                                                              |         compareAndSwapInt(Node@1,12,2,0): true at ConcurrencyOptimalTreeMap$Node.compareAndSetStateStamp(ConcurrencyOptimalTreeMap.java:64)      |
|                                                                                                                                              |     undoValidateAndTryLock(Node@1,true) at ConcurrencyOptimalTreeMap.putIfAbsent(ConcurrencyOptimalTreeMap.java:487)                             |
|                                                                                                                                              |       unlockWriteLeft() at ConcurrencyOptimalTreeMap.undoValidateAndTryLock(ConcurrencyOptimalTreeMap.java:396)                                  |
|                                                                                                                                              |         lStamp.WRITE(0) at ConcurrencyOptimalTreeMap$Node.unlockWriteLeft(ConcurrencyOptimalTreeMap.java:295)                                    |
|                                                                                                                                              |   result: null                                                                                                                                   |
|                                                                                                                                              |   thread is finished                                                                                                                             |
```

The tree consists of one dummy vertex, which is `ROOT`.
The second thread comes, realizes that it wants to insert its node as the left child of `ROOT`, takes the lock on the left edge of `ROOT` and switches.
The first thread, using the first `traverse` call, finds its `Window`, that is, the place where it wants to insert its value.
`Window` has three fields: `ggprev`, `prev` and `cur`.
As a result of the `traverse` call, `ggprev` and `cur` are null, `prev` equals `ROOT`.
These are the correct values, and this is how it should be.
Then it tries to take the lock on the left edge of `ROOT`, fails and gets here:
```
if (prev.deleted) {
    window.reset();
} else {
    window.set(window.prev, window.gprev);
}
```
Since `ROOT` is not deleted, thread falls into else, where an obviously erroneous action occurs: now `gprev` and `prev` are null, and `cur` is equal to `ROOT`.
Judging by the code as a whole, the authors wanted to preserve the invariant that either null or a real (not fictitious) node lies in `cur`.
In this line, this invariant was violated.
Then the thread started doing everything all over again, starting the second iteration of the loop.
There it called `traverse` for the second time, where there is a line:
```
comparison = k.compareTo(curr.key);
```
The `key` of the dummy node is `null`, so `compareTo` threw a `NullPointerException`.
This is an incorrect behavior of the algorithm.

For more information check [IntIntConcurrencyOptimalTreeMap](IntIntConcurrencyOptimalTreeMap).