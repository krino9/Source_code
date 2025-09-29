import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.preprocessing import MinMaxScaler
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import LSTM, Dense, Dropout
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
import os
import random


# 设置随机种子，保证结果可复现
def set_seed(seed=42):
    np.random.seed(seed)
    random.seed(seed)
    os.environ['PYTHONHASHSEED'] = str(seed)
    # tensorflow的随机种子设置
    try:
        import tensorflow as tf
        tf.random.set_seed(seed)
    except ImportError:
        pass


set_seed()


class IndustrialRULPredictor:
    def __init__(self, sequence_length=50, n_features=14, model_path='best_rul_model.h5'):
        """
        初始化工业设备剩余寿命预测器
        :param sequence_length: 时序序列长度
        :param n_features: 特征数量
        :param model_path: 模型保存路径
        """
        self.sequence_length = sequence_length
        self.n_features = n_features
        self.model_path = model_path
        self.scaler = MinMaxScaler(feature_range=(0, 1))
        self.model = None

    def load_data(self, file_path):
        """
        加载数据
        :param file_path: 数据文件路径
        :return: 加载的数据
        """
        try:
            # 假设数据格式为CSV，包含设备ID、时间步和传感器数据
            data = pd.read_csv(file_path)
            print(f"成功加载数据，形状: {data.shape}")
            return data
        except Exception as e:
            print(f"加载数据出错: {str(e)}")
            return None

    def preprocess_data(self, data):
        """
        数据预处理
        :param data: 原始数据
        :return: 处理后的训练集和测试集
        """
        if data is None:
            return None

        # 提取设备ID列表
        engines = data['engine_id'].unique()
        print(f"设备数量: {len(engines)}")

        # 按设备ID和时间步排序
        data = data.sort_values(['engine_id', 'time_step'])

        # 特征缩放
        sensor_columns = [col for col in data.columns if 'sensor' in col]
        data[sensor_columns] = self.scaler.fit_transform(data[sensor_columns])

        # 生成时序序列和对应的RUL标签
        X, y = [], []

        for engine in engines:
            engine_data = data[data['engine_id'] == engine]
            rul_values = engine_data['rul'].values
            sensor_data = engine_data[sensor_columns].values

            # 生成时序序列
            for i in range(len(engine_data) - self.sequence_length):
                X.append(sensor_data[i:i + self.sequence_length])
                # 取序列结束时的RUL作为标签
                y.append(rul_values[i + self.sequence_length - 1])

        X = np.array(X)
        y = np.array(y)

        # 划分训练集和测试集
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42
        )

        print(f"训练集形状: X={X_train.shape}, y={y_train.shape}")
        print(f"测试集形状: X={X_test.shape}, y={y_test.shape}")

        return X_train, X_test, y_train, y_test

    def build_model(self):
        """
        构建LSTM模型
        """
        model = Sequential()

        # 第一个LSTM层
        model.add(LSTM(units=64, return_sequences=True,
                       input_shape=(self.sequence_length, self.n_features)))
        model.add(Dropout(0.2))

        # 第二个LSTM层
        model.add(LSTM(units=32, return_sequences=False))
        model.add(Dropout(0.2))

        # 全连接层
        model.add(Dense(units=16, activation='relu'))
        model.add(Dense(units=1))  # 输出RUL预测值

        # 编译模型
        model.compile(optimizer='adam', loss='mean_squared_error')

        self.model = model
        print("模型构建完成")
        return model

    def train_model(self, X_train, y_train, X_val, y_val, epochs=100, batch_size=32):
        """
        训练模型
        :param X_train: 训练特征
        :param y_train: 训练标签
        :param X_val: 验证特征
        :param y_val: 验证标签
        :param epochs: 训练轮数
        :param batch_size: 批次大小
        :return: 训练历史
        """
        if self.model is None:
            self.build_model()

        # 早停策略，防止过拟合
        early_stopping = EarlyStopping(
            monitor='val_loss', patience=10, restore_best_weights=True
        )

        # 保存最佳模型
        model_checkpoint = ModelCheckpoint(
            self.model_path, monitor='val_loss', save_best_only=True
        )

        # 训练模型
        history = self.model.fit(
            X_train, y_train,
            epochs=epochs,
            batch_size=batch_size,
            validation_data=(X_val, y_val),
            callbacks=[early_stopping, model_checkpoint],
            verbose=1
        )

        return history

    def evaluate_model(self, X_test, y_test):
        """
        评估模型
        :param X_test: 测试特征
        :param y_test: 测试标签
        :return: 评估指标
        """
        if self.model is None:
            try:
                from tensorflow.keras.models import load_model
                self.model = load_model(self.model_path)
                print("已加载预训练模型")
            except Exception as e:
                print(f"加载模型失败: {str(e)}")
                return None

        # 预测
        y_pred = self.model.predict(X_test)

        # 计算评估指标
        mse = mean_squared_error(y_test, y_pred)
        rmse = np.sqrt(mse)
        mae = mean_absolute_error(y_test, y_pred)
        r2 = r2_score(y_test, y_pred)

        print(f"模型评估结果:")
        print(f"RMSE: {rmse:.4f}")
        print(f"MAE: {mae:.4f}")
        print(f"R² 分数: {r2:.4f}")

        # 绘制预测结果与真实值对比图
        plt.figure(figsize=(12, 6))
        plt.plot(y_test[:100], label='真实RUL', color='blue')
        plt.plot(y_pred[:100], label='预测RUL', color='red', linestyle='--')
        plt.title('设备剩余寿命预测 vs 真实值')
        plt.xlabel('样本索引')
        plt.ylabel('剩余寿命')
        plt.legend()
        plt.grid(True)
        plt.savefig('rul_prediction_vs_actual.png')
        plt.close()

        # 绘制误差分布直方图
        errors = y_test - y_pred.flatten()
        plt.figure(figsize=(10, 6))
        plt.hist(errors, bins=30, alpha=0.7, color='green')
        plt.axvline(x=0, color='red', linestyle='--')
        plt.title('预测误差分布')
        plt.xlabel('误差 (真实值 - 预测值)')
        plt.ylabel('频数')
        plt.grid(True, alpha=0.3)
        plt.savefig('prediction_errors_histogram.png')
        plt.close()

        return {
            'rmse': rmse,
            'mae': mae,
            'r2': r2,
            'y_true': y_test,
            'y_pred': y_pred
        }

    def predict(self, new_data):
        """
        预测新数据的RUL
        :param new_data: 新的传感器数据序列
        :return: 预测的RUL值
        """
        if self.model is None:
            try:
                from tensorflow.keras.models import load_model
                self.model = load_model(self.model_path)
            except Exception as e:
                print(f"加载模型失败: {str(e)}")
                return None

        # 数据预处理
        new_data_scaled = self.scaler.transform(new_data)

        # 确保输入形状正确
        if len(new_data_scaled.shape) == 2:
            new_data_scaled = new_data_scaled.reshape(
                1, self.sequence_length, self.n_features
            )

        # 预测
        prediction = self.model.predict(new_data_scaled)
        return prediction[0][0]


def main():
    # 创建预测器实例
    rul_predictor = IndustrialRULPredictor(sequence_length=50, n_features=14)

    # 注意：这里使用模拟数据进行演示
    # 实际应用中应替换为真实的工业设备传感器数据文件路径
    print("生成模拟数据...")
    num_engines = 100  # 设备数量
    max_time_steps = 300  # 每个设备的最大时间步

    # 生成模拟数据
    data = []
    for engine_id in range(1, num_engines + 1):
        # 每个设备的生命周期随机
        time_steps = np.random.randint(150, max_time_steps)
        for time_step in range(1, time_steps + 1):
            # 生成14个传感器的模拟数据
            sensors = np.random.rand(14)

            # 生成RUL (剩余寿命)，随时间递减
            rul = (time_steps - time_step)

            # 添加一些噪声使RUL预测更具挑战性
            rul += np.random.normal(0, 5)
            rul = max(0, rul)  # RUL不能为负

            # 组合成一行数据
            row = [engine_id, time_step] + sensors.tolist() + [rul]
            data.append(row)

    # 创建DataFrame
    columns = ['engine_id', 'time_step'] + [f'sensor_{i}' for i in range(1, 15)] + ['rul']
    simulated_data = pd.DataFrame(data, columns=columns)

    # 数据预处理
    X_train, X_test, y_train, y_test = rul_predictor.preprocess_data(simulated_data)

    if X_train is None:
        print("数据预处理失败，程序退出")
        return

    # 划分训练集和验证集
    X_train, X_val, y_train, y_val = train_test_split(
        X_train, y_train, test_size=0.2, random_state=42
    )

    # 构建并训练模型
    print("开始训练模型...")
    history = rul_predictor.train_model(X_train, y_train, X_val, y_val, epochs=50, batch_size=32)

    # 绘制训练损失曲线
    plt.figure(figsize=(10, 6))
    plt.plot(history.history['loss'], label='训练损失')
    plt.plot(history.history['val_loss'], label='验证损失')
    plt.title('模型训练损失')
    plt.xlabel('Epoch')
    plt.ylabel('Loss')
    plt.legend()
    plt.grid(True)
    plt.savefig('training_loss.png')
    plt.close()

    # 评估模型
    print("评估模型性能...")
    evaluation = rul_predictor.evaluate_model(X_test, y_test)

    # 随机选择一个测试样本进行预测演示
    if evaluation:
        sample_idx = np.random.randint(0, len(X_test))
        sample = X_test[sample_idx]
        true_rul = y_test[sample_idx]
        pred_rul = rul_predictor.predict(sample)
        print(f"\n示例预测:")
        print(f"真实剩余寿命: {true_rul:.2f}")
        print(f"预测剩余寿命: {pred_rul:.2f}")
        print(f"误差: {abs(true_rul - pred_rul):.2f}")


if __name__ == "__main__":
    main()
