import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 模块设计得好不好，评判标准之一是看这个模块对于其他模块而言，是否隐藏了其内部的实现细节。
 * 良好的模块设计会对其他模块隐藏其所有关于内部实现的细节，仅仅通过API与其他模块通信。
 * 这个概念被称为“信息隐藏”(information hiding)或者“封装”(encapsulation).
 *
 * 信息隐藏是很重要的，通过信息隐藏，可以将大多数模块解耦，更好地开发、测试、优化、复用、理解，
 * 这提高了开发速度，模块间得以并行开发。信息隐藏虽然本身不能提高性能，但它十分便于性能优化:
 * 系统完成后，可以很容易地分析出哪些模块的性能比较慢，然后在不对其他模块功能造成影响的前提下，
 * 对这些性能有待提高的模块进行优化。而且这种方式大大降低了开发大型程序的风险，即使系统整体而言是不可用的，
 * 但其中相对独立的一个个模块仍然可以被用在之后的开发工作中。
 *
 * Java提供了很多信息隐藏的机制，比如可以通过访问控制机制(access control)指定类、接口和成员的可访问性。
 * 实体的可访问性会受到其所在类与其声明语句中访问修饰符(private , protected , public)的限制。
 * 通过这些修饰符可以简洁快速地搞定信息隐藏。
 * 信息隐藏的原则也很简单，即尽量使每个类或类中的成员可访问范围尽可能地小。
 *
 *      1.对于顶层类(top-level classes)，即非嵌套的类来说，只有两种可访问级别：package-private
 *      (不加访问修饰符)和public.如果顶级类或者接口可以被设计为package-private，
 *      那么就改成package-private型。这样做的话，这个类就成为了这个包功能实现的一部分，
 *      而不作为对外API.这样的话，如果需要在之后的版本修改、替换、删除它，则无需担心影响客户端程序。
 *      而如果你想把它设置为public，那么就应该有责任永远支持它，以保证其兼容性。
 *
 *      2.如果一个package-private的顶层类或顶层接口仅被一个类所使用，则可以把它改为静态私有嵌套类，
 *      放在调用它的类里面，以缩小其可访问范围。
 *
 * 如果上面两种情况有冲突时，首先考虑将public的类改为package-private，因为package-private
 * 的类已经是这个包中功能实现的一部分了，但public的类还不是，因此将public的类改为package-private
 * 所带来的收益要高一些。
 *
 * 对于类的组成成分（成员变量，方法，内部类，内部接口），有四种可访问级别：
 *      private--仅能从声明它的顶层类访问
 *      package-private--除了interface以外，如果不加访问修饰符，那么该组成成分默认为package-private.
 *      (interface默认为public)，可以在该它所在包中的任意位置调用
 *      protected--可以在它所在的包中或者它的子类中访问它
 *      public--可以在任意位置访问它
 *
 * 仔细地设计过类的公有API之后，可能会感觉应该把所有其他成员全都置为私有的，
 * 仅当同一个包内的另一个类需要访问一个成员的时候，才应删掉private关键字，把它变为package-private型。
 * 如果发现自己一直在做这种事，那么很可能系统设计存在问题，因为包中类之间耦合太高了，应当尝试换一种分解方式。
 * 综上，private和package-private型成员都是类实现的一部分，不会影响到其导出API.但是，如果类implements
 * 了{@link java.io.Serializable}接口，那么这些字段有可能会"泄漏"到API中
 *
 * 对于public型类的组成成分来说，访问修饰符从package-private更改到protected，会导致可访问性大幅度扩大，
 * 而且更改为protected型以后，该组成成分也需要被视为对外API的一部分，需要去更新它以保证其兼容性；
 * 导出类的protected型组成成分也代表了该类对某实现细节的公开承诺。为方便更新，
 * protected型组成成分应尽量少用。
 *
 * 有一条关键规则限制了降低方法可访问范围的能力：如果在子类中覆盖了超类的一个方法，
 * 那么该方法可访问范围不能比父类中对应的方法小。这样可以保证所有能使用父类的地方都能够使用子类。
 * 不过也有特殊情况，即implements某接口时，所有接口中方法的实现必须是公有的，因为接口方法的默认访问级别是
 * public型。
 *
 * 不可以为了方便测试而改变其访问等级，尤其是像private改public这种。不过也无需这么做，
 * 因为测试类可以直接建在某功能对应的package中，这样就基本不需要更改访问等级了。
 *
 * public型类的实例域不可以是public型。如果非final型成员变量或者指向可变对象的final型引用
 * 被设置成public型，则无法保护这个成员变量不被修改，这就无法保证线程安全。就算该成员变量是final型，
 * 且引用了不可变对象，把这种成员变量设置成public型也意味着放弃了 方便修改其内部数据结构 的灵活性。
 * 这条建议同样也适用于static型成员变量。但是有一种例外：常量。
 * 通常会把一些常用的常量用public static final 修饰，放在类中以便使用，比如{@link #EXAMPLE_CONSTANT}
 * 这种常量要么是基本类型的值，要么是不可变对象的引用。这里需要特别小心，因为如果常量指向了可变成员，
 * 那么final修饰符效果尽失，可能会导致灾难性的后果。
 *
 * 长度非零的数组总是可变的，因此不要把数组设置成public static final，或者返回一个对它的访问。否则，
 * 客户端就能够任意修改数组中的内容，这是一种常见的安全漏洞。{@link #modifyFinalArray}
 * 并且，一些IDE会生成返回指向private型数组的引用的访问方法，这也会产生同样的问题。
 *
 * 要修正这个问题有两种方式:
 *      1.将public型数组改为private型并添加public型不可变访问列表，如：{@link #LIST},
 *      {@link #PRIVATE_EXAMPLE_ARRAY}
 *      2.将public型数组改为private型并添加public型的，访问私有数据副本的访问方法，
 *      如：{@link #valueOfArray}
 * 对这两者方式进行选择时，需要考虑客户端拿到数据后都干些什么，哪种更为方便，哪种性能更好。
 *
 * 从Java9开始，会加入两个新的隐式访问级别，作为模块系统(module system)的一部分被引入。
 * 一个模块(module)就是对package的一个划分，可以在该模块的声明(module declaration)中，
 * 显式地导出它的一些package（为了方便，模块声明一般会放在一个叫module-info.java的源文件中）。
 * 而public型或者protected型组成成分如果位于该module的未导出包中，那么仍然无法在模块之外访问它们，
 * 而在该模块内部则不会受到影响。
 * 通过模块系统，非导出型包(unexported package)的public型类中public和protected型组成成分，
 * 实际上被赋予了两个与普通public型、protected型类似的，隐含的可访问级别。不过这种奇怪的需求比较少，
 * 一般可以通过重新组织包中的类来消除这种需求。
 *
 * 与其他四种访问级别不同，这两种基于模块的访问级别很大程度上是咨询性的(advisory)，
 * 如果把模块的jar文件放在应用程序的类路径上，而不是其模块路径，那么模块中的包(package)
 * 将恢复其非模块行为，两种隐含访问级将会恢复为正常的public和protected，不再看是否位于导出包中。
 * 新引入的访问级别在JDK本身中被严格执行，Java类库中的非导出包在其模块之外是绝对无法访问的。
 *
 * 这种机制通过访问保护与较强的建议性(largely advisory)限制对类的访问，需要将包划分成模块，
 * 将所有依赖项显式地声明在模块中，重构source tree, 然后采取一些措施去适配所有来自模块内部、
 * 对非模块化的包的访问。
 * 目前来看，这项技术的相关规范还不太成熟，因此除迫切需要外，应当尽量避免使用它。
 *
 * @author LightDance
 */
public class MinimizeAccessibility {

    public static final int EXAMPLE_CONSTANT = 25;

    public static final int [] EXAMPLE_ARRAY = {2 , 3};

    private static final Integer [] PRIVATE_EXAMPLE_ARRAY = {3,4,5};

    /**基本类型的数组不能作为asList()的参数，因为使用了varargs和泛型，而基本数据类型不能够泛型化*/
    public static final List<Integer> LIST = Collections
            .unmodifiableList(Arrays.asList(PRIVATE_EXAMPLE_ARRAY));

    /**用于说明对数组这种引用对象，即使设置了final也可以被任意修改*/
    public static void modifyFinalArray() {
        System.out.println(EXAMPLE_ARRAY[0]);
        EXAMPLE_ARRAY[0] = 6;
        System.out.println(EXAMPLE_ARRAY[0]);
    }

    /**借助clone方法返回一个私有数组成员变量的副本*/
    public static Integer[] valueOfArray(){
        return PRIVATE_EXAMPLE_ARRAY.clone();
    }
}
