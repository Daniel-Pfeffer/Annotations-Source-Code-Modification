package social.xperience.common

sealed class FunctionVerifierClass {
    class One<A>(val a: A)
    class Two<A, B>(val a: A, val b: B)
    class Three<A, B, C>(val a: A, val b: B, val c: C)
    class Four<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
    class Five<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
    class Six<A, B, C, D, E, F>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F)
    class Seven<A, B, C, D, E, F, G>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G)
    class Eight<A, B, C, D, E, F, G, H>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H)
    class Nine<A, B, C, D, E, F, G, H, I>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H, val i: I)
    class Ten<A, B, C, D, E, F, G, H, I, J>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H, val i: I, val j: J)
    class Elven<A, B, C, D, E, F, G, H, I, J, K>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H, val i: I, val j: J, val k: K)
    class Twelve<A, B, C, D, E, F, G, H, I, J, K, L>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H, val i: I, val j: J, val k: K, val l: L)
    class Thirteen<A, B, C, D, E, F, G, H, I, J, K, L, M>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H, val i: I, val j: J, val k: K, val l: L, val m: M)
    class Fourteen<A, B, C, D, E, F, G, H, I, J, K, L, M, N>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H, val i: I, val j: J, val k: K, val l: L, val m: M, val n: N)
    class Fifteen<A, B, C, D, E, F, G, H, I, J, K, L, M, N, O>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H, val i: I, val j: J, val k: K, val l: L, val m: M, val n: N, val o: O)
}
