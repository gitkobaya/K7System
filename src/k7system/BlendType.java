package k7system;

/** 半透明時の合成方法です */
public enum BlendType {
    NOT,  // そもそも半透明ではない
    BLEND,  // 平均化
    ADDING,    // 加算
    REVERSE,// 反転
}
