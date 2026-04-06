import {
  CONDITION_LEVELS,
  ORDER_STATUS,
  AUTH_STATUS,
  PRODUCT_STATUS
} from './constant'

const campuses = [
  { id: 1, name: '南海北', code: 'nanhai_north' },
  { id: 2, name: '南海南', code: 'nanhai_south' },
  { id: 3, name: '新港', code: 'xingang' }
]

const meetingPoints = {
  1: [
    { id: 1, name: '一饭门口' },
    { id: 2, name: '图书馆门口' },
    { id: 3, name: '南门快递站' }
  ],
  2: [
    { id: 4, name: '食堂门口' },
    { id: 5, name: '操场看台' }
  ],
  3: [
    { id: 6, name: '教学楼前广场' },
    { id: 7, name: '地铁站口' }
  ]
}

const currentUser = {
  id: 10001,
  nickName: '王同学',
  avatarUrl: '/static/pic/校徽.png',
  phone: '138****8000',
  gender: 1,
  campusId: 1,
  campusName: '南海北',
  authStatus: AUTH_STATUS.VERIFIED,
  score: 4.9,
  status: 1
}

const sellerZhang = {
  id: 10002,
  nickName: '张同学',
  avatarUrl: '/static/pic/校徽.png',
  authStatus: AUTH_STATUS.VERIFIED,
  score: 4.8
}

const sellerLi = {
  id: 10003,
  nickName: '李同学',
  avatarUrl: '/static/pic/校徽.png',
  authStatus: AUTH_STATUS.PENDING,
  score: 4.6
}

const sellerChen = {
  id: 10004,
  nickName: '陈同学',
  avatarUrl: '/static/pic/校徽.png',
  authStatus: AUTH_STATUS.VERIFIED,
  score: 4.7
}

const products = [
  {
    id: 10086,
    title: 'iPad Air 4 64G 天蓝色',
    description: '2022年购入，使用频率低，几乎全新，含原装配件和包装盒。',
    price: 2800,
    originalPrice: 4799,
    categoryId: 4,
    categoryName: '电子设备',
    conditionLevel: 3,
    conditionText: '9成新',
    coverImage: '/static/pic/耳机.png',
    images: [
      '/static/pic/耳机.png',
      '/static/pic/耳机.png'
    ],
    campusId: 1,
    campusName: '南海北',
    meetingPointName: '一饭门口',
    viewCount: 234,
    favoriteCount: 12,
    status: PRODUCT_STATUS.ON_SALE,
    createTime: '2025-02-05 14:30:00',
    seller: currentUser
  },
  {
    id: 10087,
    title: '高数教材+笔记合集',
    description: '大一高数教材，配套笔记和习题册，适合备考期末和升本。',
    price: 60,
    originalPrice: 128,
    categoryId: 1,
    categoryName: '书籍',
    conditionLevel: 2,
    conditionText: '几乎全新(99新)',
    coverImage: '/static/pic/高数教材.png',
    images: ['/static/pic/高数教材.png'],
    campusId: 2,
    campusName: '南海南',
    meetingPointName: '食堂门口',
    viewCount: 98,
    favoriteCount: 35,
    status: PRODUCT_STATUS.ON_SALE,
    createTime: '2025-02-04 10:20:00',
    seller: sellerZhang
  },
  {
    id: 10088,
    title: '山地自行车 九成新',
    description: '暑假买的山地车，偶尔骑行，配有车锁和头盔。',
    price: 500,
    originalPrice: 1399,
    categoryId: 5,
    categoryName: '运动设备',
    conditionLevel: 3,
    conditionText: '9成新',
    coverImage: '/static/pic/自行车.png',
    images: ['/static/pic/自行车.png'],
    campusId: 1,
    campusName: '南海北',
    meetingPointName: '南门快递站',
    viewCount: 156,
    favoriteCount: 20,
    status: PRODUCT_STATUS.ON_SALE,
    createTime: '2025-02-03 16:00:00',
    seller: sellerLi
  },
  {
    id: 10089,
    title: '索尼降噪耳机 WH-1000XM4',
    description: '研磨时必备，降噪效果好，音质优秀，含收纳盒。',
    price: 1200,
    originalPrice: 2299,
    categoryId: 4,
    categoryName: '电子设备',
    conditionLevel: 2,
    conditionText: '几乎全新(99新)',
    coverImage: '/static/pic/耳机.png',
    images: ['/static/pic/耳机.png'],
    campusId: 3,
    campusName: '新港',
    meetingPointName: '教学楼前广场',
    viewCount: 321,
    favoriteCount: 45,
    status: PRODUCT_STATUS.ON_SALE,
    createTime: '2025-02-02 19:45:00',
    seller: currentUser
  },
  {
    id: 10090,
    title: '宿舍学习台灯 带插座',
    description: '暖光护眼，亮度可调，底座带两个插座和USB接口。',
    price: 45,
    originalPrice: 129,
    categoryId: 3,
    categoryName: '生活',
    conditionLevel: 4,
    conditionText: '8成新',
    coverImage: '/static/pic/台灯.png',
    images: ['/static/pic/台灯.png'],
    campusId: 1,
    campusName: '南海北',
    meetingPointName: '图书馆门口',
    viewCount: 67,
    favoriteCount: 10,
    status: PRODUCT_STATUS.ON_SALE,
    createTime: '2025-02-01 09:15:00',
    seller: sellerZhang
  },
  {
    id: 10091,
    title: '羽绒服 M码 适合南方冬天',
    description: '上学期买的羽绒服，今年准备换风格，保暖效果好。',
    price: 180,
    originalPrice: 399,
    categoryId: 2,
    categoryName: '服饰',
    conditionLevel: 3,
    conditionText: '9成新',
    coverImage: '/static/pic/羽绒服.png',
    images: ['/static/pic/羽绒服.png'],
    campusId: 2,
    campusName: '南海南',
    meetingPointName: '操场看台',
    viewCount: 54,
    favoriteCount: 8,
    status: PRODUCT_STATUS.ON_SALE,
    createTime: '2025-01-30 11:00:00',
    seller: sellerLi
  }
]

const favoriteProductIds = [10086, 10089]

const orders = [
  {
    id: 20001,
    orderNo: 'TD20250205143000001234',
    productId: 10086,
    productTitle: 'iPad Air 4 64G 天蓝色',
    productImage: '/static/pic/校徽.png',
    price: 2600,
    status: ORDER_STATUS.PENDING,
    statusText: '待面交',
    campusName: '南海北',
    meetingPoint: '一饭门口',
    expireTime: '2025-02-08 14:30:00',
    createTime: '2025-02-05 14:30:00',
    buyerId: currentUser.id,
    sellerId: sellerZhang.id,
    buyer: currentUser,
    seller: sellerZhang,
    otherUser: sellerZhang
  },
  {
    id: 20002,
    orderNo: 'TD20250204120000005678',
    productId: 10087,
    productTitle: '高数教材+笔记合集',
    productImage: '/static/pic/校徽.png',
    price: 60,
    status: ORDER_STATUS.COMPLETED,
    statusText: '已完成',
    campusName: '南海南',
    meetingPoint: '食堂门口',
    expireTime: '2025-02-07 12:00:00',
    createTime: '2025-02-04 12:00:00',
    buyerId: currentUser.id,
    sellerId: sellerLi.id,
    buyer: currentUser,
    seller: sellerLi,
    otherUser: sellerLi
  },
  {
    id: 20003,
    orderNo: 'TD20250203160000001235',
    productId: 10088,
    productTitle: '山地自行车 九成新',
    productImage: '/static/pic/校徽.png',
    price: 480,
    status: ORDER_STATUS.REVIEWED,
    statusText: '已评价',
    campusName: '南海北',
    meetingPoint: '南门快递站',
    expireTime: '2025-02-06 16:00:00',
    createTime: '2025-02-03 16:00:00',
    buyerId: sellerZhang.id,
    sellerId: currentUser.id,
    buyer: sellerZhang,
    seller: currentUser,
    otherUser: sellerZhang
  },
  {
    id: 20004,
    orderNo: 'TD20250202194500001236',
    productId: 10089,
    productTitle: '索尼降噪耳机 WH-1000XM4',
    productImage: '/static/pic/校徽.png',
    price: 1180,
    status: ORDER_STATUS.CANCELLED,
    statusText: '已取消',
    campusName: '新港',
    meetingPoint: '教学楼前广场',
    expireTime: '2025-02-05 19:45:00',
    createTime: '2025-02-02 19:45:00',
    buyerId: sellerLi.id,
    sellerId: currentUser.id,
    buyer: sellerLi,
    seller: currentUser,
    otherUser: sellerLi
  }
]

const notifications = [
  {
    id: 1,
    category: 1,
    type: 2,
    title: '新的交易消息',
    content: '您有一笔新的订单待确认，请尽快与对方联系。',
    createTime: '2025-02-05 15:00:00',
    isRead: false,
    relatedType: 2,
    relatedId: 20001
  },
  {
    id: 2,
    category: 1,
    type: 1,
    title: '订单已完成',
    content: '与张同学的订单已完成，记得去评价哦。',
    createTime: '2025-02-04 18:20:00',
    isRead: false,
    relatedType: 2,
    relatedId: 20002
  },
  {
    id: 3,
    category: 2,
    type: 3,
    title: '认证通过',
    content: '您的校园认证已通过，快去发布第一件闲置吧。',
    createTime: '2025-02-03 09:30:00',
    isRead: true,
    relatedType: 3,
    relatedId: null
  },
  {
    id: 4,
    category: 2,
    type: 4,
    title: '系统通知',
    content: '平台近期优化了搜索功能，欢迎体验。',
    createTime: '2025-02-02 12:00:00',
    isRead: true,
    relatedType: 4,
    relatedId: null
  },
  {
    id: 5,
    category: 1,
    type: 2,
    title: '待面交提醒',
    content: '与李同学的面交时间临近，请提前确认地点和时间。',
    createTime: '2025-02-01 20:10:00',
    isRead: false,
    relatedType: 2,
    relatedId: 20003
  }
]

const chatSessions = [
  {
    id: 50001,
    userId: sellerZhang.id,
    nickName: sellerZhang.nickName,
    avatarUrl: sellerZhang.avatarUrl,
    authStatus: sellerZhang.authStatus,
    lastMessage: '可以今晚七点在一饭门口吗？',
    lastTime: '2025-02-05 16:20:00',
    unread: 2,
    productId: 10087,
    productTitle: '高数教材+笔记合集',
    productPrice: 60,
    productImage: '/static/pic/高数教材.png'
  },
  {
    id: 50002,
    userId: sellerLi.id,
    nickName: sellerLi.nickName,
    avatarUrl: sellerLi.avatarUrl,
    authStatus: sellerLi.authStatus,
    lastMessage: '我这边还有收纳袋，可以一起给你。',
    lastTime: '2025-02-05 14:50:00',
    unread: 0,
    productId: 10088,
    productTitle: '山地自行车 九成新',
    productPrice: 500,
    productImage: '/static/pic/自行车.png'
  },
  {
    id: 50003,
    userId: sellerChen.id,
    nickName: sellerChen.nickName,
    avatarUrl: sellerChen.avatarUrl,
    authStatus: sellerChen.authStatus,
    lastMessage: '已帮你留好，明天中午可以吗？',
    lastTime: '2025-02-04 21:05:00',
    unread: 3,
    productId: 10090,
    productTitle: '宿舍学习台灯 带插座',
    productPrice: 45,
    productImage: '/static/pic/台灯.png'
  },
  {
    id: 50004,
    userId: currentUser.id,
    nickName: currentUser.nickName,
    avatarUrl: currentUser.avatarUrl,
    authStatus: currentUser.authStatus,
    lastMessage: '我会提前到达地点。',
    lastTime: '2025-02-03 09:40:00',
    unread: 1,
    productId: 10089,
    productTitle: '索尼降噪耳机 WH-1000XM4',
    productPrice: 1200,
    productImage: '/static/pic/耳机.png'
  }
]

const categories = [
  { id: 1, name: '书籍', icon: '📚', iconPath: '/static/svg/book.svg' },
  { id: 2, name: '服饰', icon: '👕', iconPath: '/static/svg/clothing.svg' },
  { id: 3, name: '生活设备', icon: '🏠', iconPath: '/static/svg/water-bottle.svg' },
  { id: 4, name: '电子设备', icon: '📱', iconPath: '/static/svg/electronics.svg' },
  { id: 5, name: '运动设备', icon: '⚽', iconPath: '/static/svg/sports.svg' },
  { id: 6, name: '潮玩娱乐', icon: '🎮', iconPath: '/static/svg/hobby.svg' },
  { id: 7, name: '快递', icon: '📦', iconPath: '/static/svg/bicycle.svg' }
]

const favoriteProducts = products.filter((item) =>
  favoriteProductIds.includes(item.id)
)

const sellerProducts = (sellerId) =>
  products.filter((item) => item.seller && item.seller.id === sellerId)

const myProducts = sellerProducts(currentUser.id).map((item, index) => {
  let status = PRODUCT_STATUS.ON_SALE
  if (index === 0) {
    status = PRODUCT_STATUS.ON_SALE
  } else if (index === 1) {
    status = PRODUCT_STATUS.PENDING
  } else if (index === 2) {
    status = PRODUCT_STATUS.SOLD
  } else if (index === 3) {
    status = PRODUCT_STATUS.OFF_SHELF
  } else if (index === 4) {
    status = PRODUCT_STATUS.REJECTED
  }
  return {
    ...item,
    status
  }
})

const buildProfileData = (seller, stats = {}) => {
  const list = sellerProducts(seller.id)
  const onSaleCount = stats.onSaleCount ?? list.length
  const soldCount = stats.soldCount ?? 0
  return {
    id: seller.id,
    nickName: seller.nickName,
    avatarUrl: seller.avatarUrl,
    authStatus: seller.authStatus,
    score: seller.score,
    onSaleCount,
    soldCount,
    products: {
      total: list.length,
      records: list
    }
  }
}

const productDetailMocks = products.reduce((result, item) => {
  result[`GET /mini/product/detail/${item.id}`] = {
    code: 1,
    msg: 'success',
    data: {
      ...item,
      isFavorited: favoriteProductIds.includes(item.id),
      isOwner: item.seller && item.seller.id === currentUser.id,
      hasActiveOrder: false
    }
  }
  return result
}, {})

const favoriteCheckMocks = products.reduce((result, item) => {
  result[`GET /mini/favorite/check/${item.id}`] = {
    code: 1,
    msg: 'success',
    data: {
      isFavorited: favoriteProductIds.includes(item.id)
    }
  }
  return result
}, {})

const orderDetailMocks = orders.reduce((result, order) => {
  const product = products.find((item) => item.id === order.productId) || products[0]
  result[`GET /mini/order/detail/${order.id}`] = {
    code: 1,
    msg: 'success',
    data: {
      id: order.id,
      orderNo: order.orderNo,
      price: order.price,
      status: order.status,
      statusText: order.statusText,
      campusName: order.campusName,
      meetingPoint: order.meetingPoint,
      expireTime: order.expireTime,
      createTime: order.createTime,
      product,
      buyer: order.buyer,
      seller: order.seller
    }
  }
  return result
}, {})

const reviewDetailMocks = orders.reduce((result, order, index) => {
  if (order.status !== ORDER_STATUS.REVIEWED) return result
  const product = products.find((item) => item.id === order.productId) || products[0]
  const scoreBase = 4 + (index % 2)
  result[`GET /mini/review/detail/${order.id}`] = {
    code: 1,
    msg: 'success',
    data: {
      orderId: order.id,
      scoreDesc: scoreBase,
      scoreAttitude: scoreBase,
      scoreExperience: Math.max(3, scoreBase - 1),
      content: '整体体验不错，沟通顺畅，商品符合描述。',
      createTime: order.createTime,
      fromUser: order.buyer,
      toUser: order.seller,
      product: {
        id: product.id,
        title: product.title,
        coverImage: product.coverImage,
        price: product.price
      }
    }
  }
  return result
}, {})

export const mockData = {
  'POST /mini/user/wx-login': {
    code: 1,
    msg: 'success',
    data: {
      token: 'mock-token-10001',
      userInfo: currentUser
    }
  },
  'POST /mini/user/login': {
    code: 1,
    msg: 'success',
    data: {
      token: 'mock-token-10001',
      userInfo: currentUser
    }
  },
  'POST /mini/user/sms-login': {
    code: 1,
    msg: 'success',
    data: {
      token: 'mock-token-10001',
      userInfo: currentUser
    }
  },
  'POST /mini/user/sms/send': {
    code: 1,
    msg: 'success',
    data: null
  },
  'GET /mini/user/info': {
    code: 1,
    msg: 'success',
    data: currentUser
  },
  'POST /mini/user/update': {
    code: 1,
    msg: 'success',
    data: currentUser
  },
  'POST /mini/user/deactivate': {
    code: 1,
    msg: 'success',
    data: null
  },
  'GET /mini/user/stats': {
    code: 1,
    msg: 'success',
    data: {
      onSaleCount: 12,
      soldCount: 28,
      favoriteCount: 56
    }
  },
  'GET /mini/user/profile/10001': {
    code: 1,
    msg: 'success',
    data: buildProfileData(currentUser, { onSaleCount: 12, soldCount: 28 })
  },
  'GET /mini/user/profile/10002': {
    code: 1,
    msg: 'success',
    data: buildProfileData(sellerZhang, { onSaleCount: 6, soldCount: 12 })
  },
  'GET /mini/user/profile/10003': {
    code: 1,
    msg: 'success',
    data: buildProfileData(sellerLi, { onSaleCount: 5, soldCount: 9 })
  },
  'POST /mini/auth/submit': {
    code: 1,
    msg: 'success',
    data: null
  },
  'GET /mini/auth/status': {
    code: 1,
    msg: 'success',
    data: {
      status: AUTH_STATUS.VERIFIED,
      remark: '审核通过'
    }
  },
  'GET /mini/college/list': {
    code: 1,
    msg: 'success',
    data: [
      { id: 1, name: '计算机学院' },
      { id: 2, name: '机电学院' },
      { id: 3, name: '艺术设计学院' },
      { id: 4, name: '经济管理学院' }
    ]
  },
  'GET /mini/product/list': {
    code: 1,
    msg: 'success',
    data: {
      total: products.length,
      records: products
    }
  },
  ...productDetailMocks,
  'GET /mini/product/my-list': {
    code: 1,
    msg: 'success',
    data: {
      total: myProducts.length,
      records: myProducts
    }
  },
  'POST /mini/product/publish': {
    code: 1,
    msg: 'success',
    data: {
      productId: 10092
    }
  },
  'POST /mini/product/update': {
    code: 1,
    msg: 'success',
    data: null
  },
  'POST /mini/product/update-price': {
    code: 1,
    msg: 'success',
    data: null
  },
  'POST /mini/product/off-shelf': {
    code: 1,
    msg: 'success',
    data: null
  },
  'POST /mini/product/on-shelf': {
    code: 1,
    msg: 'success',
    data: null
  },
  'GET /mini/category/list': {
    code: 1,
    msg: 'success',
    data: categories
  },
  'GET /mini/campus/list': {
    code: 1,
    msg: 'success',
    data: campuses
  },
  'GET /mini/campus/meeting-points/1': {
    code: 1,
    msg: 'success',
    data: meetingPoints[1]
  },
  'GET /mini/campus/meeting-points/2': {
    code: 1,
    msg: 'success',
    data: meetingPoints[2]
  },
  'GET /mini/campus/meeting-points/3': {
    code: 1,
    msg: 'success',
    data: meetingPoints[3]
  },
  'GET /mini/banner/list': {
    code: 1,
    msg: 'success',
    data: [
      {
        id: 1,
        title: '学长学姐闲置大甩卖(南海)',
        image: '/static/pic/推广（南海北）.webp',
        linkType: 2,
        linkUrl: '/pages/search/search'
      },
       {
        id: 2,
        title: '学长学姐闲置大甩卖(广州)',
        image: '/static/pic/推广（广州）.webp',
        linkType: 1,
        linkUrl: '/pages/search/search'
      }
    ]
  },
  'POST /mini/favorite/add': {
    code: 1,
    msg: 'success',
    data: null
  },
  'POST /mini/favorite/cancel': {
    code: 1,
    msg: 'success',
    data: null
  },
  'GET /mini/favorite/list': {
    code: 1,
    msg: 'success',
    data: favoriteProducts
  },
  ...favoriteCheckMocks,
  'POST /mini/order/create': {
    code: 1,
    msg: 'success',
    data: {
      orderId: 1,
      orderNo: 'TD20250205143000001234',
      expireTime: '2025-02-08 14:30:00'
    }
  },
  'GET /mini/order/list': {
    code: 1,
    msg: 'success',
    data: {
      total: orders.length,
      records: orders
    }
  },
  ...orderDetailMocks,
  'POST /mini/order/confirm': {
    code: 1,
    msg: 'success',
    data: null
  },
  'POST /mini/order/cancel': {
    code: 1,
    msg: 'success',
    data: null
  },
  'POST /mini/order/delete': {
    code: 1,
    msg: 'success',
    data: null
  },
  'POST /mini/review/submit': {
    code: 1,
    msg: 'success',
    data: null
  },
  ...reviewDetailMocks,
  'POST /mini/report/submit': {
    code: 1,
    msg: 'success',
    data: null
  },
  'GET /mini/notification/list': {
    code: 1,
    msg: 'success',
    data: {
      total: notifications.length,
      records: notifications
    }
  },
  'GET /mini/notification/unread-count': {
    code: 1,
    msg: 'success',
    data: {
      total: 5,
      trade: 3,
      system: 2
    }
  },
  'POST /mini/notification/read': {
    code: 1,
    msg: 'success',
    data: null
  },
  'POST /mini/notification/read-all': {
    code: 1,
    msg: 'success',
    data: null
  },
  'GET /mini/chat/list': {
    code: 1,
    msg: 'success',
    data: {
      total: chatSessions.length,
      records: chatSessions
    }
  },
  'POST /mini/chat/delete': {
    code: 1,
    msg: 'success',
    data: null
  },
  'GET /mini/search/hot-keywords': {
    code: 1,
    msg: 'success',
    data: [
      { keyword: '升本英语' },
      { keyword: '计算器' },
      { keyword: '台灯' },
      { keyword: '自行车' },
      { keyword: '教材' },
      { keyword: '耳机' },
      { keyword: '洗衣机' },
      { keyword: '升本政治' }
    ]
  },
  'POST /common/upload': {
    code: 1,
    msg: 'success',
    data: {
      url: '/static/pic/校徽.png'
    }
  }
}
