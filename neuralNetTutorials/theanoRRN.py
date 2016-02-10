import numpy as np
import theano
import theano.tensor as T
import matplotlib

nn_input_dim = 7
nn_hdim = 10
nn_output_dim = 7

X = T.matrix('X')
y = T.matrix('y')

def initialize_weight(sizeX, sizeY):
    return np.random.randn(sizeX, sizeY)

def initialize_params(n_in, n_h, n_out):
    h_0 = theano.shared(np.zeros(n_h))
    W_ih = theano.shared(initialize_weight(n_in, n_h))
    W_hh = theano.shared(initialize_weight(n_h, n_h))
    b_h = theano.shared(np.zeros(n_h))
    W_ho = theano.shared(initialize_weight(n_h, n_out))
    b_o = theano.shared(np.zeros(n_out))
    return h_0, W_ih, W_hh, b_h, W_ho, b_o

h_0, W_ih, W_hh, b_h, W_ho, b_o = initialize_params(nn_input_dim,
                                                    nn_hdim,
                                                    nn_output_dim)
params = [h_0, W_ih, W_hh, b_h, W_ho, b_o]

def one_step(x_t, h_t_prev, W_ih, W_hh, b_h, W_ho, b_o):
    z_h_t = x_t.dot(W_ih) + h_t_prev.dot(W_hh) + b_h
    # h_t = T.tanh(z_h_t)
    h_t = T.nnet.sigmoid(z_h_t)
    z_o = h_t.dot(W_ho) + b_o
    # resize output since softmax returns a 2D matrix even on 1D input
    # output = T.nnet.softmax(z_o).dimshuffle(1,)
    output = T.nnet.sigmoid(z_o)
    return [h_t, output]

# forward propagation is just a scan over the input using the one_step function
[h_vals, y_hat], updates = theano.scan(fn=one_step,
                                 sequences=X,
                # use h_0 as the initial y as well just because we don't need it
                                 outputs_info=[h_0, None],
                                 non_sequences=[W_ih, W_hh, b_h, W_ho, b_o]
                                 )

# Gradient descent parameters
epsilon = 0.2

loss = T.nnet.categorical_crossentropy(y_hat, y).mean()

def get_train_functions(cost, x, target):
    gparams = []
    for param in params:
        gparam = T.grad(cost, param)
        gparams.append(param)

    updates = []
    for param, gparam in zip(params, gparams):
        updates.append((param, param - gparam * epsilon))
    learn_rnn_fn = theano.function(inputs = [x, target], outputs=cost,
                                    updates=updates)
    return learn_rnn_fn

learn_rnn_fn = get_train_functions(loss, X, y)

import reberGrammar
train_data = reberGrammar.get_n_examples(1000)

def train_rnn(train_data, epochs=50):
    train_errors = np.zeros(epochs)
    for x in range(epochs):
        error = 0.
        for j in range(len(train_data)):
            index = np.random.randint(0, len(train_data))
            i, o = train_data[index]
            train_cost = learn_rnn_fn(i, o)
            error += train_cost
        train_errors[x] = error
    return train_errors

nb_epochs=100
train_errors = train_rnn(train_data, nb_epochs)

print train_errors

import matplotlib.pyplot as plt
def plot_learning_curve(train_errors):
    plt.plot(np.arange(nb_epochs), train_errors, 'b-')
    plt.xlabel('epochs')
    plt.ylabel('error')
plot_learning_curve(train_errors)
